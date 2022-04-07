package foo.bar.example.forecoroutine.feature.counter

data class CounterState(val isBusy: Boolean = false, val count: Int = 0, val progress: Int = 0)

data class CombinedState(val counterState: CounterState, val counterWithProgressState: CounterState)
