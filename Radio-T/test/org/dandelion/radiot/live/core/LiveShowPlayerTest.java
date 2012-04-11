package org.dandelion.radiot.live.core;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class LiveShowPlayerTest {
    private final LiveShowStateHolder stateHolder = LiveShowStateHolder.initial();
    private final AudioStream audioStream = mock(AudioStream.class);
    private final Scheduler schedule = mock(Scheduler.class);
    private final PlayerActivityListener activityListener = mock(PlayerActivityListener.class);

    private LiveShowPlayer player;

    private AudioStream.Listener stateListener;

    @Before
    public void setUp() throws Exception {
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                stateListener = (AudioStream.Listener) invocation.getArguments()[0];
                return null;
            }
        }).when(audioStream).setStateListener(any(AudioStream.Listener.class));

        player = new LiveShowPlayer(audioStream, stateHolder, schedule);
        player.setActivityListener(activityListener);
    }

    @Test
    public void goesConnecting() throws Exception {
        player.beConnecting();
        assertIsConnecting();
    }

    @Test
    public void goesStopping() throws Exception {
        player.beStopping();
        assertIsStopping();
    }

    @Test
    public void goesPlayingWhenPrepared() throws Exception {
        player.beConnecting();
        stateListener.onStarted();
        assertIsPlaying();
    }

    @Test
    public void schedulesTheNextAttemptIfConnectFails() throws Exception {
        player.beConnecting();
        stateListener.onError();
        assertCurrentStateIs(LiveShowState.Waiting);
        verify(schedule).scheduleNextAttempt();
    }

    @Test
    public void goesConnectingOnPlayingError() throws Exception {
        player.onStarted();
        stateListener.onError();
        assertIsConnecting();
    }

    @Test
    public void goesIdleOnStopped() throws Exception {
        player.beStopping();
        stateListener.onStopped();
        assertCurrentStateIs(LiveShowState.Idle);
        verify(activityListener).onDeactivated();
    }

    @Test
    public void performNextConnectionAttempt() throws Exception {
        player.beWaiting();
        player.performNextAttempt();
        assertIsConnecting();
    }

    @Test
    public void cancelsConnectionAttemptsWhenGoesIdle() throws Exception {
        player.beIdle();
        verify(schedule).cancelAttempts();
    }

    @Test
    public void normalPlaybackWorkflow() throws Exception {
        player.togglePlayback();
        assertIsConnecting();
        stateListener.onStarted();
        assertIsPlaying();

        player.togglePlayback();
        assertIsStopping();
        stateListener.onStopped();
        assertIsIdle();
    }

    @Test
    public void waitingForShowWorkflow() throws Exception {
        player.togglePlayback();
        assertIsConnecting();
        stateListener.onError();
        assertCurrentStateIs(LiveShowState.Waiting);
    }

    private void assertIsIdle() {
        assertCurrentStateIs(LiveShowState.Idle);
    }

    private void assertIsConnecting() throws IOException {
        verify(audioStream).play();
        assertCurrentStateIs(LiveShowState.Connecting);
    }

    private void assertIsStopping() {
        verify(audioStream).stop();
        assertCurrentStateIs(LiveShowState.Stopping);
    }

    private void assertIsPlaying() {
        LiveShowState expected = LiveShowState.Playing;
        assertCurrentStateIs(expected);
    }

    private void assertCurrentStateIs(LiveShowState expected) {
        assertEquals("Current state", stateHolder.value(), expected);
    }
}
