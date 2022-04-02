package gmk57.helpers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

// for cold/warm Flows better use other APIs, see https://medium.com/androiddevelopers/a-safer-way-to-collect-flows-from-android-uis-23080b1f8bda
fun <T> StateFlow<T>.observe(lifecycleOwner: LifecycleOwner, action: FlowCollector<T>) =
    lifecycleOwner.lifecycleScope.launchWhenStarted { collect(action) }
