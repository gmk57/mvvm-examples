package gmk57.helpers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

// for cold/warm Flows better use other APIs, see https://medium.com/androiddevelopers/a-safer-way-to-collect-flows-from-android-uis-23080b1f8bda
fun <T> Flow<T>.observe(lifecycleOwner: LifecycleOwner, action: FlowCollector<T>) =
    lifecycleOwner.lifecycleScope.launchWhenStarted { collect(action) }

var appScope: CoroutineScope = CoroutineScope(SupervisorJob())

var backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
