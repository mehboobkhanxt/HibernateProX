package com.hibernateprox.app.core.engine

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class HibernatorAccessibilityService : AccessibilityService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ServiceEntryPoint {
        fun foregroundAppTracker(): ForegroundAppTracker
        fun forceStopCoordinator(): AccessibilityForceStopCoordinator
    }

    private val entryPoint: ServiceEntryPoint by lazy {
        EntryPointAccessors.fromApplication(applicationContext, ServiceEntryPoint::class.java)
    }

    @Volatile
    private var lastActionTimeMs: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        entryPoint.foregroundAppTracker().setServiceConnected(true)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val eventPkg = event.packageName?.toString() ?: return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            entryPoint.foregroundAppTracker().onForegroundWindowChanged(eventPkg)
        }

        // Strict privacy guard: never traverse window hierarchy unless a force-stop target is active
        // AND the active window belongs to a known system Settings package.
        val coordinator = entryPoint.forceStopCoordinator()
        if (coordinator.currentTargetPackage == null ||
            !OemForceStopDictionary.isSettingsPackage(eventPkg)
        ) {
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastActionTimeMs < ACTION_DEBOUNCE_MS) return

        val root = findSettingsWindowRoot() ?: return
        coordinator.onSettingsWindowEntered()

        if (coordinator.isWaitingForConfirm) {
            val confirmNode = findConfirmButton(root)
            if (confirmNode != null && clickNodeOrClickableParent(confirmNode)) {
                lastActionTimeMs = now
                coordinator.onConfirmClicked()
                performGlobalAction(GLOBAL_ACTION_BACK)
                return
            }
        }

        val forceStopNode = findForceStopButton(root) ?: return
        if (!isNodeOrParentEnabled(forceStopNode)) {
            lastActionTimeMs = now
            coordinator.onForceStopDisabledDetected()
            performGlobalAction(GLOBAL_ACTION_BACK)
            return
        }

        if (clickNodeOrClickableParent(forceStopNode)) {
            lastActionTimeMs = now
            coordinator.onForceStopClicked()
        }
    }

    override fun onInterrupt() {
        entryPoint.forceStopCoordinator().cancelActive()
    }

    override fun onDestroy() {
        entryPoint.foregroundAppTracker().setServiceConnected(false)
        super.onDestroy()
    }

    private fun findForceStopButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        for (viewId in OemForceStopDictionary.forceStopViewIds) {
            val byId = root.findAccessibilityNodeInfosByViewId(viewId)
            val match = byId?.firstOrNull { node ->
                OemForceStopDictionary.matchesForceStopText(node.text) ||
                    OemForceStopDictionary.matchesForceStopText(node.contentDescription) ||
                    OemForceStopDictionary.matchesForceStopId(node.viewIdResourceName)
            }
            if (match != null) return match
        }
        return findNodeDfs(root) { node ->
            OemForceStopDictionary.matchesForceStopText(node.text) ||
                OemForceStopDictionary.matchesForceStopText(node.contentDescription)
        }
    }

    private fun findConfirmButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        for (viewId in OemForceStopDictionary.confirmDialogViewIds) {
            val byId = root.findAccessibilityNodeInfosByViewId(viewId)
            val match = byId?.firstOrNull { node ->
                node.isEnabled && (
                    OemForceStopDictionary.matchesConfirmText(node.text) ||
                        OemForceStopDictionary.matchesConfirmId(node.viewIdResourceName)
                    )
            }
            if (match != null) return match
        }
        return findNodeDfs(root) { node ->
            node.isEnabled && OemForceStopDictionary.matchesConfirmText(node.text)
        }
    }

    private fun findNodeDfs(
        node: AccessibilityNodeInfo?,
        predicate: (AccessibilityNodeInfo) -> Boolean,
    ): AccessibilityNodeInfo? {
        if (node == null) return null
        if (predicate(node)) return node
        for (i in 0 until node.childCount) {
            val found = findNodeDfs(node.getChild(i), predicate)
            if (found != null) return found
        }
        return null
    }

    private fun findSettingsWindowRoot(): AccessibilityNodeInfo? {
        val activeRoot = rootInActiveWindow
        val activePkg = activeRoot?.packageName?.toString()
        if (OemForceStopDictionary.isSettingsPackage(activePkg)) {
            return activeRoot
        }
        return windows.orEmpty()
            .asSequence()
            .mapNotNull { it.root }
            .firstOrNull { OemForceStopDictionary.isSettingsPackage(it.packageName?.toString()) }
    }

    private fun isNodeOrParentEnabled(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        var depth = 0
        while (current != null && depth < 4) {
            if (current.isClickable) return current.isEnabled
            current = current.parent
            depth++
        }
        return node.isEnabled
    }

    private fun clickNodeOrClickableParent(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        var depth = 0
        while (current != null && depth < 4) {
            if (current.isClickable && current.isEnabled) {
                return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            current = current.parent
            depth++
        }
        return false
    }

    companion object {
        private const val ACTION_DEBOUNCE_MS = 180L
    }
}
