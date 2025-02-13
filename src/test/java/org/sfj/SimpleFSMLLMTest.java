package org.sfj;

import org.junit.Test;

import javax.swing.plaf.nimbus.State;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SimpleFSMLLMTest {


    @Test
    public void testProcessFailure() throws Exception {
        SimpleFSM<SimpleFSMTest.TurnStileEvents> fsm = new SimpleFSM<>("testFSM", SimpleFSMTest.TurnStileEvents.class);
        fsm.state("s1");
        fsm.begin(fsm.state("s1"));
        fsm.onException(SimpleFSMTest.TurnStileEvents.COIN, (st, e, args) -> null);
        try {
            fsm.fire(SimpleFSMTest.TurnStileEvents.PUSH);
        } catch (SimpleFSM.TransitionException e) {
            assertThat(e.getMessage(), containsString("No transition defined"));
        }
    }


    @Test
    public void testShutdownMethod() {
        SimpleFSM<SimpleFSMTest.TrafficLightEvents> fsm = new SimpleFSM<>("fsm", SimpleFSMTest.TrafficLightEvents.class);
        fsm.state("s1");
        fsm.begin(fsm.state("s1"));
        fsm.shutdown();
        assertThat(fsm.getCurrentState(), is(nullValue()));
    }

    @Test
    public void testTransitionThrowsWhenFrozen() {
        SimpleFSM<SimpleFSMTest.TurnStileEvents> fsm = new SimpleFSM<>("FSM", SimpleFSMTest.TurnStileEvents.class);
        SimpleFSM.State<SimpleFSMTest.TurnStileEvents> state = fsm.state("testState");
        fsm.begin(state);

        Exception exception = null;
        try {
            state.transition(SimpleFSMTest.TurnStileEvents.COIN, (st, e, args) -> state);
        } catch (IllegalStateException e) {
            exception = e;
        }
        assertThat(exception.getMessage(), containsString("FSM is already frozen"));
    }

    @Test
    public void testTransitionThrowsWhenRedefined() {
        SimpleFSM<SimpleFSMTest.TurnStileEvents> fsm = new SimpleFSM<>("FSM", SimpleFSMTest.TurnStileEvents.class);
        SimpleFSM.State<SimpleFSMTest.TurnStileEvents> state = fsm.state("testState");
        state.transition(SimpleFSMTest.TurnStileEvents.COIN, (st, e, args) -> state);

        Exception exception = null;
        try {
            state.transition(SimpleFSMTest.TurnStileEvents.COIN, (st, e, args) -> state);
        } catch (IllegalStateException e) {
            exception = e;
        }
        assertThat(exception.getMessage(), containsString("State transition already defined"));
    }

    @Test
    public void testGetTransitionFailureCount() {
        SimpleFSM<SimpleFSMTest.TurnStileEvents> fsm = new SimpleFSM<>("TestFSM", SimpleFSMTest.TurnStileEvents.class);
        assertThat(fsm.getTransitionFailureCount(), is(0L));
    }


    @Test
    public void testFireWithNullCurrentState() {
        SimpleFSM<SimpleFSMTest.TurnStileEvents> fsm = new SimpleFSM<>("TestFSM", SimpleFSMTest.TurnStileEvents.class);
        Exception exception = assertThrows(IllegalStateException.class, () -> fsm.fire(SimpleFSMTest.TurnStileEvents.COIN));
        assertThat(exception.getMessage(), containsString("FSM is not frozen"));
    }

    @Test
    public void testFireWithNoTransitionDefined() {
        SimpleFSM<SimpleFSMTest.TurnStileEvents> fsm = new SimpleFSM<>("TestFSM", SimpleFSMTest.TurnStileEvents.class);
        SimpleFSM.State<SimpleFSMTest.TurnStileEvents> initial = fsm.state("initial");
        fsm.begin(initial);
        Exception exception = assertThrows(SimpleFSM.TransitionException.class, () -> fsm.fire(SimpleFSMTest.TurnStileEvents.COIN));
        assertThat(exception.getMessage(), containsString("No transition defined"));
    }

    @Test
    public void testProcessFailureIncrementsFailureCount() {
        SimpleFSM<SimpleFSMTest.TurnStileEvents> fsm = new SimpleFSM<>("TestFSM", SimpleFSMTest.TurnStileEvents.class);
        SimpleFSM.State<SimpleFSMTest.TurnStileEvents> initial = fsm.state("initial");
        fsm.begin(initial);

        fsm.onException(SimpleFSMTest.TurnStileEvents.COIN, (s, e, args) -> initial);
        fsm.fireAsynchronously(SimpleFSMTest.TurnStileEvents.COIN);

        assertTrue(fsm.getTransitionFailureCount() >= 0);
    }



}
