package com.ikota.voiceinputsample;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class SRTest extends ActivityInstrumentationTestCase2<MyListActivity>{

    public SRTest() {
        super(MyListActivity.class);
    }

    private Context context;
    private MediaPlayer player;

    @Rule
    public ActivityTestRule<MyListActivity> activityRule = new ActivityTestRule<>(
            MyListActivity.class,
            true,     // initialTouchMode
            false);   // launchActivity. False so we can customize the intent per test method

    @Before
    public void setUp() throws Exception {
        super.setUp();
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        context = instrumentation.getTargetContext();
    }

    @After
    public void cleanUp() {
        if(player != null) {
            if(player.isPlaying()) {
                player.stop();
            }
            player = null;
        }
    }

    @Test
    public void precondition() {
        activityRule.launchActivity(new Intent());
        onView(withId(R.id.fab)).check(matches(isListening(false)));
        onView(allOf(withId(R.id.text), hasSibling(allOf(withId(R.id.num), withText("0"))))).check(matches(withText("There is always light behind the clouds.")));
        onView(withId(android.R.id.list)).perform(RecyclerViewActions.scrollToPosition(19));
        onView(allOf(withId(R.id.text), hasSibling(allOf(withId(R.id.num), withText("19"))))).check(matches(withText("Move fast and break things. ")));
    }

    @Test
    public void clickState() {
        MyListActivity activity = activityRule.launchActivity(new Intent());
        RecyclerView rv = (RecyclerView)activity.findViewById(android.R.id.list);
        Item item0 = ((MyListAdapter)rv.getAdapter()).getItemAt(0);
        Item item1 = ((MyListAdapter)rv.getAdapter()).getItemAt(1);
        assertTrue(item0.selected);
        assertFalse(item1.selected);
        onView(withId(android.R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        assertTrue(item1.selected);
        assertFalse(item0.selected);
    }

    @Test
    public void next() {
        MyListActivity activity = activityRule.launchActivity(new Intent());
        RecyclerView rv = (RecyclerView)activity.findViewById(android.R.id.list);
        Item item0 = ((MyListAdapter)rv.getAdapter()).getItemAt(0);
        Item item1 = ((MyListAdapter)rv.getAdapter()).getItemAt(1);
        assertTrue(item0.selected);
        assertFalse(item1.selected);
        onView(withId(R.id.fab)).perform(click());
        SystemClock.sleep(5000);
        player = MediaPlayer.create(context, R.raw.next);
        player.start();
        SystemClock.sleep(5000);
        assertTrue(item1.selected);
        assertFalse(item0.selected);
    }

    @Test
    public void above() {
        MyListActivity activity = activityRule.launchActivity(new Intent());
        RecyclerView rv = (RecyclerView)activity.findViewById(android.R.id.list);
        Item item0 = ((MyListAdapter)rv.getAdapter()).getItemAt(0);
        Item item1 = ((MyListAdapter)rv.getAdapter()).getItemAt(1);
        assertTrue(item0.selected);
        assertFalse(item1.selected);
        onView(withId(android.R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        onView(withId(R.id.fab)).perform(click());
        SystemClock.sleep(5000);
        player = MediaPlayer.create(context, R.raw.above);
        player.start();
        SystemClock.sleep(5000);
        assertTrue(item0.selected);
        assertFalse(item1.selected);
    }

    @Test
    public void fifteen() {
        MyListActivity activity = activityRule.launchActivity(new Intent());
        RecyclerView rv = (RecyclerView)activity.findViewById(android.R.id.list);
        Item item0 = ((MyListAdapter)rv.getAdapter()).getItemAt(0);
        Item item15 = ((MyListAdapter)rv.getAdapter()).getItemAt(15);
        onView(withId(R.id.fab)).perform(click());
        SystemClock.sleep(5000);
        player = MediaPlayer.create(context, R.raw.fifteen);
        player.start();
        SystemClock.sleep(5000);
        assertTrue(item15.selected);
        assertFalse(item0.selected);
    }

    @Test
    public void bottom() {
        MyListActivity activity = activityRule.launchActivity(new Intent());
        RecyclerView rv = (RecyclerView)activity.findViewById(android.R.id.list);
        Item item0 = ((MyListAdapter)rv.getAdapter()).getItemAt(0);
        Item item19 = ((MyListAdapter)rv.getAdapter()).getItemAt(19);
        onView(withId(R.id.fab)).perform(click());
        SystemClock.sleep(5000);
        player = MediaPlayer.create(context, R.raw.bottom);
        player.start();
        SystemClock.sleep(5000);
        assertTrue(item19.selected);
        assertFalse(item0.selected);
    }

    @Test
    public void top() {
        MyListActivity activity = activityRule.launchActivity(new Intent());
        RecyclerView rv = (RecyclerView)activity.findViewById(android.R.id.list);
        Item item0 = ((MyListAdapter)rv.getAdapter()).getItemAt(0);
        Item item19 = ((MyListAdapter)rv.getAdapter()).getItemAt(19);
        onView(withId(android.R.id.list)).perform(RecyclerViewActions.scrollToPosition(19));
        onView(withId(android.R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition(19, click()));
        onView(withId(R.id.fab)).perform(click());
        SystemClock.sleep(5000);
        player = MediaPlayer.create(context, R.raw.top);
        player.start();
        SystemClock.sleep(5000);
        assertTrue(item0.selected);
        assertFalse(item19.selected);
        onView(allOf(withId(R.id.num), withText("0"))).check(matches(isDisplayed()));
    }

    @Test
    public void finish() {
        activityRule.launchActivity(new Intent());
        onView(withId(R.id.fab)).check(matches(isListening(false)));
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.fab)).check(matches(isListening(true)));
        SystemClock.sleep(5000);
        player = MediaPlayer.create(context, R.raw.finish);
        player.start();
        SystemClock.sleep(3000);
        onView(withId(R.id.fab)).check(matches(isListening(false)));
    }

    @Test
    public void allCommands() {
        MyListActivity activity = activityRule.launchActivity(new Intent());
        RecyclerView rv = (RecyclerView)activity.findViewById(android.R.id.list);
        List<Item> items = ((MyListAdapter)rv.getAdapter()).getItems();
        // click
        onView(withId(android.R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        SystemClock.sleep(4000);
        // start voice command
        onView(withId(R.id.fab)).perform(click());
        SystemClock.sleep(5000);
        // next
        player = MediaPlayer.create(context, R.raw.next);
        player.start();
        SystemClock.sleep(5000);
        assertFalse(items.get(0).selected);
        assertTrue(items.get(1).selected);
        // next
        player = MediaPlayer.create(context, R.raw.next);
        player.start();
        SystemClock.sleep(5000);
        assertFalse(items.get(1).selected);
        assertTrue(items.get(2).selected);
        // above
        player = MediaPlayer.create(context, R.raw.above);
        player.start();
        SystemClock.sleep(7000);
        assertFalse(items.get(2).selected);
        assertTrue(items.get(1).selected);
        // fifteen
        player = MediaPlayer.create(context, R.raw.fifteen);
        player.start();
        SystemClock.sleep(7000);
        assertFalse(items.get(1).selected);
        assertTrue(items.get(15).selected);
        // top
        player = MediaPlayer.create(context, R.raw.top);
        player.start();
        SystemClock.sleep(5000);
        assertFalse(items.get(15).selected);
        assertTrue(items.get(0).selected);
        // bottom
        player = MediaPlayer.create(context, R.raw.bottom);
        player.start();
        SystemClock.sleep(7000);
        assertFalse(items.get(0).selected);
        assertTrue(items.get(19).selected);
        // finish
        player = MediaPlayer.create(context, R.raw.finish);
        player.start();
        SystemClock.sleep(3000);
        onView(withId(R.id.fab)).check(matches(isListening(false)));
    }


    public static Matcher<View> isListening(final boolean state) {
        final Matcher<Boolean> matcher = is(state);
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                return (boolean)item.getTag() == state;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is listening :");
                matcher.describeTo(description);
            }
        };
    }

}
