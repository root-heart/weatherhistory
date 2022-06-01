package rootheart.codes.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext

@DelicateCoroutinesApi
class ConcurrentExecutor(concurrency: Int, name: String) {
    private val scope = CoroutineScope(newFixedThreadPoolContext(concurrency, name))
    private val jobs = ArrayList<Job>()

    fun run(block: () -> Unit) {
        jobs += scope.launch { block() }
    }

    suspend fun awaitCompletion() {
        jobs.joinAll()
    }
}