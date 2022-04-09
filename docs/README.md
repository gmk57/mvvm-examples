# MVVM examples

This is an attempt to show and compare various approaches to writing declarative & reactive view layer on Android.

## Motivation

The idea of it came after reading [a great series of articles](https://dev.to/erdo/tutorial-spot-the-deliberate-bug-165k) on this topic by Eric Donovan, please have a look to better understand what follows. His main point might seem obvious, and personally, I've been using a similar approach for many years, but I've seen developers struggling with it, writing imperative UI code with a lot of boilerplate and creeping bugs. So it certainly deserves more guidance.

A few months ago Google's Android team published a new [Guide to app architecture](https://developer.android.com/jetpack/guide), centered on Unidirectional data flow (UDF). It is a big step forward, probably inspired by Jetpack Compose, but the same principles can and should be followed with classic Android View system, and Google's guide lacks specific examples of how to do that. Moreover, Google [recommends cold/warm Flows and special collectors](https://medium.com/androiddevelopers/a-safer-way-to-collect-flows-from-android-uis-23080b1f8bda) to implicitly propagate view lifecycle to domain layer, which [I think is not a good idea](https://gmk57.medium.com/thanks-you-have-a-very-good-point-23a394b44a66) because of increased complexity and hidden coupling of domain layer to view implementation details.

## Starting point

Technically, this repository is a fork of Eric's [fore library](https://github.com/erdo/android-fore). I've chosen it as a starting point because it contains various sample apps, seemingly custom-tailored to my goals:
1) They are simple enough to see easily what's going on, but realistic enough to show the techniques applicable to real apps
1) View itself is already written in declarative & reactive style, with a single `syncView()` function
1) The code has a good test coverage

## StateFlow

On the other hand, for non-Compose apps I prefer to implement Observer pattern via `StateFlow` from Kotlin Coroutines, which has many useful properties:
* It can be observed ;)
* Its behavior does not depend on the presence and count of observers
* It caches the latest value
* It has a built-in conflation/deduplication (like `distinctUntilChanged()` in a regular `Flow`)
* It can be updated atomically with recently added `update()` method
* Its `value` can be queried synchronously, which is very handy in some cases
* It is able to pass `null` values (in contrast to RxJava) and respects declared nullability (in contrast to `LiveData`)
* There is a ready-made `stateIn()` for converting any `Flow` to `StateFlow`

Typically ViewModel (or any other class that should be observed) exposes all its state in a `StateFlow<SomeState>`, where `SomeState` is a data class.
Compared to fore-based approach, there are some differences:
1) No need to wire observation manually: to call `notifyObservers()`, `removeObserver()`, or call `syncView()` again after `addObserver()`. Less boilerplate, but more importantly, less error-prone: no chances to forget adding these calls.
1) Updating state may be slightly more verbose (though often it's still one-liner), but on the other side, we don't need getters or `private set` for every single piece of state. Hopefully it will be more convenient when/if Kotlin gets [value classes](https://github.com/Kotlin/KEEP/blob/master/notes/value-classes.md).
1) `StateFlow.update()` is thread-safe & atomic. This is not always important, but again, lowers chances of bugs.

## What I've changed

I'm migrating Kotlin versions of sample apps one by one, so hopefully the list below will grow over time.

The main goal is to compare different approaches, from the perspective of code clarity, testability, supporting configuration changes, etc.

That's why I make as little changes as possible, and preserve some *fore* parts not related to state management (e.g. logging, service locator). For the same reason *fore* library modules and Java examples are left intact. Original fore-based implementation is kept in [original_fore](/../../tree/original_fore) branch, so you can easily diff branches on GitHub or in IDE.

### 1 Reactive UI Example: [description](https://erdo.github.io/android-fore/#fore-1-reactive-ui-example), [code](/example-kt-01reactiveui)

Parts of the `Wallet` logic were moved to `WalletState`. `WalletsActivity` got rid of wiring boilerplate.

In tests, instead of verifying that observer was called, we should check that state has changed. We can test `WalletState` separately, including cases that were harder to test previously (large numbers, `totalDollarsAvailable` != 10). `StateBuilder` was simplified, it can use regular `WalletState` instead of mocks.

### 2 Asynchronous Code Example: [description](https://erdo.github.io/android-fore/#fore-2-asynchronous-code-example), [code](/example-kt-02coroutine)

`CounterState` is extracted & reused between `Counter` and `CounterWithProgress`. Tricky part: `CounterActivity` needs data from two sources. In this particular case we could just split `syncView()` into two separate methods, but in general it may not be possible, so instead we combine two `Flow`s into one. Anyway, it's less code than before.

Production code got rid of checking `Fore.getWorkMode()` in `delay()`, `launchMain()` and `awaitDefault()`. Instead, we use `TestDispatcher` and `runTest()` from `kotlinx-coroutines-test` library to advance virtual time step by step. The only adjustment of production code specifically for testing is a `backgroundDispatcher` (here a top-level property, but normally passed via dependency injection).

## Jetpack Compose

Compose has its own "snapshot state" infrastructure, which aims to [simplify state updating and observation](https://dev.to/zachklipp/a-historical-introduction-to-the-compose-reactive-state-model-19j8), compared to `StateFlow<SomeState>`. I plan to re-implement these sample apps again in Compose to show the difference.

## License

    Copyright 2015-2022 early.co
    Copyright 2022 George Kropotkin

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
