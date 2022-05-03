package foo.bar.example.foreadapterskt.ui

import android.content.res.Resources
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher

object EspressoTestMatchers {

    /**
     * Adapted from https://stackoverflow.com/a/30361345
     */
    fun withRecyclerViewItems(size: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            public override fun matchesSafely(view: View): Boolean {
                return (view as RecyclerView).adapter!!.itemCount == size
            }

            override fun describeTo(description: Description) {
                description.appendText("RecycleView should have $size items")
            }
        }
    }

    /**
     * Returns ViewInteraction on the view inside [RecyclerView], specified by its position and
     * resource id (optional, for cases when each item contains multiple views).
     * Provides clear error messages if view can't be found.
     *
     * Adapted from from https://stackoverflow.com/a/64241245, https://stackoverflow.com/a/70008599
     * & https://stackoverflow.com/a/31475962, published as https://stackoverflow.com/a/72105818
     *
     * NB: You may need to [scrollToPosition] beforehand.
     */
    fun onRecyclerViewItem(
        recyclerViewId: Int,
        position: Int,
        targetViewId: Int? = null
    ): ViewInteraction = onView(object : TypeSafeMatcher<View>() {

        private lateinit var resources: Resources
        private var recyclerView: RecyclerView? = null
        private var holder: RecyclerView.ViewHolder? = null
        private var targetView: View? = null

        override fun describeTo(description: Description) {
            fun Int.name(): String = try {
                "R.id.${resources.getResourceEntryName(this)}"
            } catch (e: Resources.NotFoundException) {
                "unknown id $this"
            }

            val text = when {
                recyclerView == null -> "RecyclerView (${recyclerViewId.name()})"
                holder == null || targetViewId == null -> "in RecyclerView (${recyclerViewId.name()}) at position $position"
                else -> "in RecyclerView (${recyclerViewId.name()}) at position $position and with ${targetViewId.name()}"
            }
            description.appendText(text)
        }

        override fun matchesSafely(view: View): Boolean {
            // matchesSafely will be called for each view in the hierarchy (until found),
            // it makes no sense to perform lookup over and over again
            if (!::resources.isInitialized) {
                resources = view.resources
                recyclerView = view.rootView.findViewById(recyclerViewId) ?: return false
                holder = recyclerView?.findViewHolderForAdapterPosition(position) ?: return false
                targetView = holder?.itemView?.let {
                    if (targetViewId != null) it.findViewById(targetViewId) else it
                }
            }
            return view === targetView
        }
    })

    /**
     * Returns a [ViewAction] which scrolls [RecyclerView] to a specific position.
     * Copied from `androidx.test.espresso.contrib.RecyclerViewActions` for two reasons:
     * 1) drop dependency on `espresso-contrib` artifact (this is the only thing we need from there)
     * 2) drop unneeded `<VH extends ViewHolder>` type parameter
     */
    fun scrollToPosition(position: Int): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> =
            Matchers.allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())

        override fun getDescription(): String = "scroll RecyclerView to position: $position"

        override fun perform(uiController: UiController, view: View) {
            val recyclerView = view as RecyclerView
            recyclerView.scrollToPosition(position)
            uiController.loopMainThreadUntilIdle()
        }
    }
}
