package com.hana053.micropost.pages.main

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.hana053.micropost.R
import com.hana053.micropost.interactor.MicropostInteractor
import com.hana053.micropost.service.Navigator
import com.hana053.micropost.testing.*
import com.hana053.myapp.interactors.FeedInteractor
import com.nhaarman.mockito_kotlin.*
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rx.Observable


@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest : InjectableTest by InjectableTestImpl() {

    @Rule @JvmField
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun shouldBeOpenedWhenAuthenticated() {
        overrideAppBindings {
            fakeAuthToken()
            bind<FeedInteractor>(overrides = true) with instance(mock<FeedInteractor> {
                // just avoid exception
                on { loadNextFeed(anyOrNull()) } doReturn Observable.empty()
            })
        }
        activityRule.launchActivity(null)
        onView(allOf(
            isDescendantOfA(withResourceName("android:id/action_bar_container")),
            withText(R.string.home)
        ))
    }

    @Test
    fun shouldNotBeOpenedWhenNotAuthenticated() {
        val navigator = mock<Navigator>()
        overrideAppBindings {
            bind<Navigator>(overrides = true) with instance(navigator)
        }
        activityRule.launchActivity(null)
        verify(navigator).navigateToTop()
    }

    @Test
    fun shouldShowFeed() {
        overrideAppBindings {
            fakeAuthToken()
            bind<FeedInteractor>(overrides = true) with instance(mock<FeedInteractor> {
                on { loadNextFeed(null) } doReturn Observable.just(listOf(
                    TestMicropost.copy(content = "my test content")
                ))
                // just avoid exception
                on { loadPrevFeed(anyOrNull()) } doReturn Observable.empty()
            })
        }

        activityRule.launchActivity(null)

        onView(withId(R.id.list_post))
            .check(matches(atPositionOnView(0, withText("my test content"), R.id.tv_post_content)))
    }

    @Test
    fun shouldLoadNextFeedWhenSwipeRefreshed() {
        overrideAppBindings {
            fakeAuthToken()
            bind<FeedInteractor>(overrides = true) with instance(mock<FeedInteractor> {
                on { loadNextFeed(null) } doReturn Observable.just(listOf(
                    TestMicropost.copy(id = 1, content = "my test content")
                ))
                on { loadNextFeed(1) } doReturn Observable.just(listOf(
                    TestMicropost.copy(id = 2, content = "my new content")
                ))
                // just avoid exception
                on { loadPrevFeed(anyOrNull()) } doReturn Observable.empty()
            })
        }

        activityRule.launchActivity(null)
        onView(withId(R.id.swipe_refresh)).perform(swipeDown())

        onView(withId(R.id.list_post))
            .check(matches(atPositionOnView(0, withText("my new content"), R.id.tv_post_content)))
        onView(withId(R.id.list_post))
            .check(matches(atPositionOnView(1, withText("my test content"), R.id.tv_post_content)))
    }

    @Test
    fun shouldLoadPreviousFeedWhenReachedToBottom() {
        overrideAppBindings {
            fakeAuthToken()
            bind<FeedInteractor>(overrides = true) with instance(mock<FeedInteractor> {
                on { loadNextFeed(null) } doReturn Observable.just(listOf(
                    TestMicropost.copy(id = 1, content = "my test content")
                ))
                on { loadPrevFeed(1) } doReturn Observable.just(listOf(
                    TestMicropost.copy(id = 0, content = "my old content")
                ))
                // just avoid exception
                on { loadPrevFeed(0) } doReturn Observable.empty()
            })
        }

        activityRule.launchActivity(null)

        onView(withId(R.id.list_post))
            .check(matches(atPositionOnView(0, withText("my test content"), R.id.tv_post_content)))
        onView(withId(R.id.list_post))
            .check(matches(atPositionOnView(1, withText("my old content"), R.id.tv_post_content)))
    }

    @Test
    fun shouldNavigateToUserShowWhenAvatarClicked() {
        val navigator = mock<Navigator>()
        overrideAppBindings {
            fakeAuthToken()
            bind<Navigator>(overrides = true) with instance(navigator)
            bind<FeedInteractor>(overrides = true) with instance(mock<FeedInteractor> {
                on { loadNextFeed(null) } doReturn Observable.just(listOf(
                    TestMicropost.copy(id = 1, content = "my test content")
                ))
                // just avoid exception
                on { loadPrevFeed(anyOrNull()) } doReturn Observable.empty()
            })
        }

        activityRule.launchActivity(null)
        onView(withId(R.id.img_avatar)).perform(click())

        verify(navigator).navigateToUserShow(1)
    }

    @Test
    fun shouldNavigateToMicropostNewWhenNewMicropostBtnClicked() {
        val navigator = mock<Navigator>()
        overrideAppBindings {
            fakeAuthToken()
            bind<Navigator>(overrides = true) with instance(navigator)
            bind<FeedInteractor>(overrides = true) with instance(mock<FeedInteractor> {
                // just avoid exception
                on { loadNextFeed(anyOrNull()) } doReturn Observable.empty()
                on { loadPrevFeed(anyOrNull()) } doReturn Observable.empty()
            })
        }

        activityRule.launchActivity(null)
        onView(withId(R.id.btn_new_micropost)).perform(click())

        verify(navigator).navigateToMicropostNew()
    }

    @Test
    fun shouldLoadNextFeedWhenBackedFromPosting() {
        val feedInteractor: FeedInteractor = mock {
            on { loadNextFeed(anyOrNull()) } doReturn Observable.just(emptyList())
            on { loadPrevFeed(anyOrNull()) } doReturn Observable.empty()
        }
        overrideAppBindings {
            fakeAuthToken()
            bind<FeedInteractor>(overrides = true) with instance(feedInteractor)
            bind<MicropostInteractor>(overrides = true) with instance(mock<MicropostInteractor> {
                on { create(any()) } doReturn Observable.just(TestMicropost)
            })
        }

        activityRule.launchActivity(null)
        verify(feedInteractor, times(1)).loadNextFeed(null)

        onView(withId(R.id.btn_new_micropost)).perform(click())
        onView(withId(R.id.et_post)).perform(typeText("hello"))
        onView(withId(R.id.btn_post)).perform(closeSoftKeyboard(), click())

        verify(feedInteractor, times(2)).loadNextFeed(null)
    }

}