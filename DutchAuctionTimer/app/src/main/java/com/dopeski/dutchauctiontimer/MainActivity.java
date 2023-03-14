package com.dopeski.dutchauctiontimer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.dopeski.dutchauctiontimer.databinding.ActivityMainBinding;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.Range;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.RangeMap;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.TreeRangeMap;


public class MainActivity extends AppCompatActivity
{
    private final float START_LOCATION = -0f;
    private final float END_LOCATION = -315f;
    private final float END_AND_FINAL_DOUBLE_LOCATION = -214.1f;
    private final float FINAL_LOCATION = -340f;
    private final int DURATION = 27540;
    private final int DOUBLED_DURATION = 18150;
    private final String PAY_PHRASE_BEGIN = "Player must pay ";
    private final String PAY_PHRASE_END = ",000 florins";

    private float CurrentPosition = 0;
    private boolean Doubled = false;
    private boolean ResetAfterDoubled = false;

    private Animation ButtonAnimation;
    private ActivityMainBinding Binding;
    private Button ButtonStartAndReset;
    private Button ButtonStop;
    private MediaPlayer TimerSound;
    private MediaPlayer DoubledTimerSound;
    private ObjectAnimator RotateAnimation;
    private ObjectAnimator DoubledRotateAnimation;
    private ObjectAnimator CirclePulseAnimation;
    private ObjectAnimator DoubledPulseAnimation;
    private RangeMap<Float, String> NumberRange;
    private ImageView Circle;
    private ImageView Clock;
    private ImageView ClockDoubled;
    private ImageView ClockHand;
    private ImageView DoubledIcon;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SetApplicationToFullScreen();

        CurrentPosition = START_LOCATION;

        // Binds UI components in layout with data sources in app
        Binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(Binding.getRoot());

        // Numbers
        NumberRange = TreeRangeMap.create();
        NumberRange.put(Range.closed(0f, 9.7f), "0");
        NumberRange.put(Range.closed(9.7f, 29f), "200");
        NumberRange.put(Range.closed(29f, 48.05f), "190");
        NumberRange.put(Range.closed(48.05f, 66.7f), "180");
        NumberRange.put(Range.closed(66.7f, 85.4f), "170");
        NumberRange.put(Range.closed(85.4f, 103.9f), "160");
        NumberRange.put(Range.closed(103.9f, 122.2f), "150");
        NumberRange.put(Range.closed(122.2f, 140.7f), "140");
        NumberRange.put(Range.closed(140.7f, 159.2f), "130");
        NumberRange.put(Range.closed(159.2f, 177.1f), "120");
        NumberRange.put(Range.closed(177.1f, 195.5f), "110");
        NumberRange.put(Range.closed(195.5f, 214.1f), "100");
        NumberRange.put(Range.closed(214.1f, 233.7f), "90");
        NumberRange.put(Range.closed(233.7f, 253.1f), "80");
        NumberRange.put(Range.closed(253.1f, 272.9f), "70");
        NumberRange.put(Range.closed(272.9f, 293f), "60");
        NumberRange.put(Range.closed(293f, 312.6f), "50");

        // Finger
        Circle = findViewById(R.id.img_circle);
        Circle.setVisibility(View.INVISIBLE);

        // ClockHand
        ClockHand = findViewById(R.id.clock_hand);

        // Clock
        Clock = findViewById(R.id.clock);

        // Clock (Doubled)
        ClockDoubled = findViewById(R.id.clock_doubled);

        // 2X!
        DoubledIcon = findViewById(R.id.doubled);

        // Rotate Animation
        RotateAnimation = CreateRotationAnimation(ClockHand, START_LOCATION, END_LOCATION, DURATION);
        RotateAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                FinalAnimation(FINAL_LOCATION);
            }
        });

        // Rotation Animation (Doubled)
        DoubledRotateAnimation = CreateRotationAnimation(ClockHand, START_LOCATION, END_AND_FINAL_DOUBLE_LOCATION, DOUBLED_DURATION);
        DoubledRotateAnimation.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                Out(PAY_PHRASE_BEGIN + 200 + PAY_PHRASE_END);

                HideDoubledIcon();

                HideStopButtonAndShowStartResetButton();

                ResetAfterDoubled = true;
                CurrentPosition = END_AND_FINAL_DOUBLE_LOCATION;
                Doubled = false;
            }
        });

        // Timer Sound
        TimerSound = MediaPlayer.create(getApplicationContext(), R.raw.timer_full_final);

        // Timer Sound (Doubled)
        DoubledTimerSound = MediaPlayer.create(getApplicationContext(), R.raw.timer_doubled_final);


        // Start and Reset button
        ButtonStartAndReset = findViewById(R.id.button_start_reset);
        ButtonStartAndReset.setBackgroundColor(Color.TRANSPARENT);
        ButtonStartAndReset.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                StartAndResetButton();
            }
        });

        // Stop button
        ButtonStop = findViewById(R.id.button_stop);
        ButtonStop.setBackgroundColor(Color.TRANSPARENT);
        ButtonStop.setOnTouchListener((view, motionEvent) ->
        {
            int eventAction = motionEvent.getAction();
            switch (eventAction)
            {
                case MotionEvent.ACTION_DOWN:
                    StopButton(motionEvent);
                    break;
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    break;
            }

            return true;
        });
    }

    // START and RESET button
    private void StartAndResetButton() {
        if (RotateAnimation != null || DoubledRotateAnimation != null) {
            if (CurrentPosition == START_LOCATION) // Start pressed
            {
                CreateAndDoButtonPressAnimation(ClockHand);

                if (Doubled)
                {
                    DoubledTimerSound.start();
                    DoubledRotateAnimation.start();
                }
                else
                {
                    TimerSound.start();
                    RotateAnimation.start();
                }

                ButtonStartAndReset.setVisibility(View.INVISIBLE);
                ButtonStop.setVisibility(View.VISIBLE);
            }
            else if (CurrentPosition == FINAL_LOCATION || CurrentPosition == END_AND_FINAL_DOUBLE_LOCATION) // End of animation
            {
                if (ResetAfterDoubled)
                {
                    ObjectAnimator resetAnimation = CreateRotationAnimation(ClockHand, END_AND_FINAL_DOUBLE_LOCATION, -360, 300);
                    resetAnimation.start();

                    ResetAfterDoubled = false;
                    FadeOutClock(false);
                }
                else
                {
                    ObjectAnimator resetAnimation = CreateRotationAnimation(ClockHand, FINAL_LOCATION, -360, 100);
                    resetAnimation.start();
                }

                CurrentPosition = START_LOCATION;

            }
            else // Reset pressed
            {
                if (ResetAfterDoubled)
                {
                    ResetDoubledClockAnimation();
                }
                else
                {
                    ResetClockAnimation();
                }

                // Stop Circle from animating
                if (Circle.getVisibility() == View.VISIBLE)
                {
                    Circle.setAnimation(CreateFadeOutAnimation(Circle));
                    Circle.setVisibility(View.INVISIBLE);
                }

                ClockHand.clearAnimation();

                CurrentPosition = START_LOCATION;
                ResetAfterDoubled = false;
            }
        }
    }

    // STOP button
    private void StopButton(MotionEvent motionEvent)
    {
        if (Doubled)
        {
            DoubledTimerSound.pause();
            DoubledTimerSound.seekTo(0);

            DoubledRotateAnimation.pause();
        }
        else
        {
            TimerSound.pause();
            TimerSound.seekTo(0);

            RotateAnimation.pause();
        }

        ClockHand.clearAnimation();

        CreateAndDoButtonPressAnimation(ClockHand);

        CurrentPosition = (Float) RotateAnimation.getAnimatedValue();

        HideStopButtonAndShowStartResetButton();

        float currentPosition = 0.0f;

        if (Doubled)
        {
            currentPosition = (Float) DoubledRotateAnimation.getAnimatedValue();
        }
        else
        {
            currentPosition = (Float) RotateAnimation.getAnimatedValue();
        }

        // If on Start spot, DOUBLE the number
        if (currentPosition < 0 && currentPosition > -9.7f)
        {
            Out("DOUBLED!", true);

            if (DoubledPulseAnimation == null) // 2x! animation
            {
                DoubledPulseAnimation = CreatePulseAnimation(DoubledIcon, 1.05f, 500);
                DoubledPulseAnimation.start();
            }

            DoubledIcon.setVisibility(View.VISIBLE);
            FadeOutClock(true);
            Doubled = true;
        }
        else // Pay what is shown (and show green touch spot)
        {
            DisplayCircle(motionEvent);

            if (CirclePulseAnimation == null) // Pulse animation for the green touch spot
            {
                CirclePulseAnimation = CreatePulseAnimation(Circle, 1.18f, 310);
                CirclePulseAnimation.start();
            }

            int number = Integer.parseInt(NumberRange.get(-currentPosition));

            if (Doubled)
            {
                number = number * 2;

                HideDoubledIcon();

                Doubled = false;
                ResetAfterDoubled = true;
            }

            Out(PAY_PHRASE_BEGIN + number + PAY_PHRASE_END);
        }
    }

    private void HideStopButtonAndShowStartResetButton()
    {
        ButtonStop.setVisibility(View.INVISIBLE);
        ButtonStartAndReset.setVisibility(View.VISIBLE);
    }

    private void HideDoubledIcon()
    {
        DoubledIcon.clearAnimation();
        DoubledIcon.setAnimation(CreateFadeOutAnimation(DoubledIcon));
        DoubledIcon.setVisibility(View.INVISIBLE);
    }

    private void ResetClockAnimation()
    {
        float currentPosition = (Float) RotateAnimation.getAnimatedValue();
        ObjectAnimator resetAnimation = CreateRotationAnimation(ClockHand, currentPosition, 0, 200);
        resetAnimation.start();
    }

    private void ResetDoubledClockAnimation()
    {
        FadeOutClock(false);

        float currentPosition = (Float) DoubledRotateAnimation.getAnimatedValue();
        ObjectAnimator resetAnimation = CreateRotationAnimation(ClockHand, currentPosition, 0, 500);
        resetAnimation.start();

        ResetAfterDoubled = false;
    }

    private void DisplayCircle(MotionEvent motionEvent)
    {
        int X = (int) motionEvent.getX();
        int Y = (int) motionEvent.getY();

        Circle.setVisibility(View.VISIBLE);
        Circle.setX(X - 200);
        Circle.setY(Y - 200);
    }

    public AlphaAnimation CreateFadeOutAnimation(View view)
    {
        AlphaAnimation animation = new AlphaAnimation(1, 0);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(500);

        return animation;
    }

    public void CreateAndDoButtonPressAnimation(View view)
    {
        ButtonAnimation = new ScaleAnimation(
                0.95f, 1f, // Start and end values for the X axis scaling
                0.95f, 1f, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        ButtonAnimation.setFillAfter(true); // Needed to keep the result of the animation
        ButtonAnimation.setDuration(200);
        view.startAnimation(ButtonAnimation);
    }

    private ObjectAnimator CreateRotationAnimation(View viewToAnimate, float start, float end, int duration)
    {
        ObjectAnimator animation = ObjectAnimator.ofFloat(viewToAnimate, View.ROTATION, start, end);
        animation.setDuration(duration);
        animation.setRepeatCount(0);
        animation.setInterpolator(new LinearInterpolator());

        return animation;
    }

    private ObjectAnimator CreatePulseAnimation(View view, float intensity, int duration)
    {
        ObjectAnimator pulseAnimation = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat("scaleX", intensity),
                PropertyValuesHolder.ofFloat("scaleY", intensity));
        pulseAnimation.setDuration(duration);
        pulseAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        pulseAnimation.setRepeatMode(ObjectAnimator.REVERSE);

        return pulseAnimation;
    }

    private void FadeOutClock(boolean fadeClock)
    {
        if (fadeClock)
        {
            Clock.animate().alpha(0f).setDuration(500);
            ClockDoubled.animate().alpha(1.0f).setDuration(500);
        }
        else
        {
            Clock.animate().alpha(1.0f).setDuration(500);
            ClockDoubled.animate().alpha(0f).setDuration(500);
        }
    }

    // Animation plays once the Clockhand has completed it's rotation
    private void FinalAnimation(float endLocation)
    {
        Float currentPosition = (Float) RotateAnimation.getAnimatedValue();

        // Set ClockHand from end to final position
        ObjectAnimator finalAnimation = CreateRotationAnimation(ClockHand, currentPosition, endLocation, 100);
        finalAnimation.start();

        HideStopButtonAndShowStartResetButton();

        CurrentPosition = FINAL_LOCATION;
    }

    private void SetApplicationToFullScreen()
    {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.hide();
        }
    }

    //region HELPER FUNCTIONS

    private void Out(String message)
    {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void Out(String message, boolean shortLength)
    {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    //endregion
}