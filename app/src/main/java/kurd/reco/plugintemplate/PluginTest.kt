package kurd.reco.plugintemplate

import kotlinx.coroutines.runBlocking
import kurd.reco.api.ProviderTester
import kurd.reco.example.ExampleApi

fun main() {
    runBlocking {
        val tester = ProviderTester(ExampleApi())

        tester.testAll() // test all methods
    }
}