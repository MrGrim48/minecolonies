package com.minecolonies.coremod.entity.ai.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * A simple target the AI tries to accomplish.
 * It has a state matcher,
 * so it only gets executed on matching state.
 * It has a tester function to make more checks
 * to tell if execution is wanted.
 * And it can change state.
 */
public class AITarget
{

    @Nullable
    private final AIState           state;
    @NotNull
    private final BooleanSupplier   predicate;
    @NotNull
    private final Supplier<AIState> action;

    /**
     * The max delay one can set upon AITarget creation
     */
    public static final int MAX_AI_TICKRATE         = 500;
    /**
     * Maximum of the random offset for AI Ticks, to not activate on the same tick.
     */
    public static final int MAX_AI_TICKRATE_VARIANT = 50;

    /**
     * The tickrate at which the Target should be called, e.g. tickRate = 20 means call function every 20 Ticks
     */
    @NotNull
    private        int tickRate;
    /**
     * The random offset for Ticks, so that AITargets get more distributed activations on server ticks
     */
    @NotNull
    private final  int tickOffset;
    /**
     * The variant used upon creation of the AITarget to uniformly distribute the Tick offset
     * Static variable counter that changes with each AITarget creation and affects the next one.
     */
    private static int tickOffsetVariant = 0;

    /**
     * Variable describing if it is okay to eat in a state.
     */
    private boolean okayToEat;

    /**
     * Construct a target.
     * TODO: Remove once all Targets transitioned to tickRate
     * @param action the action to apply
     */
    public AITarget(@NotNull final Supplier<AIState> action, final boolean isOkayToEat)
    {
        this(() -> true, isOkayToEat, action, 1);
    }

    /**
     * Construct a target.
     *
     * @param action the action to apply
     */
    public AITarget(@NotNull final Supplier<AIState> action, final boolean isOkayToEat, @NotNull final int tickRate)
    {
        this(() -> true, isOkayToEat, action, tickRate);
    }

    /**
     * Construct a target.
     * TODO: Remove once all Targets transitioned to tickRate
     * @param predicate the predicate for execution
     * @param action    the action to apply
     */
    public AITarget(@NotNull final BooleanSupplier predicate, final boolean isOkayToEat, @NotNull final Supplier<AIState> action)
    {
        this(null, isOkayToEat, predicate, action, 1);
    }

    /**
     * Construct a target.
     *
     * @param predicate the predicate for execution
     * @param action    the action to apply
     */
    public AITarget(@NotNull final BooleanSupplier predicate, final boolean isOkayToEat, @NotNull final Supplier<AIState> action, @NotNull final int tickRate)
    {
        this(null, isOkayToEat, predicate, action, tickRate);
    }

    /**
     * Construct a target.
     * TODO: Remove once all Targets transitioned to tickRate
     * @param state     the state it needs to be | null
     * @param predicate the predicate for execution
     * @param action    the action to apply
     */
    public AITarget(
      @Nullable final AIState state,
      final boolean isOkayToEat,
      @NotNull final BooleanSupplier predicate,
      @NotNull final Supplier<AIState> action,
      @NotNull final int tickRate)
    {
        if (state == null)
        {
            this.state = AIState.STATE_BLOCKING_PRIO;
        }
        else
        {
            this.state = state;
        }
        this.predicate = predicate;
        this.action = action;
        this.okayToEat = isOkayToEat;

        // Limit rates
        this.tickRate = tickRate > MAX_AI_TICKRATE ? MAX_AI_TICKRATE : tickRate;
        this.tickRate = this.tickRate < 1 ? 1 : this.tickRate;

        // Calculate offSet % tickRate already to not have redundant calculations later
        this.tickOffset = tickOffsetVariant % this.tickRate;
        // Increase variant for next AITarget and reset variant at a certain point
        tickOffsetVariant++;
        if (tickOffsetVariant >= MAX_AI_TICKRATE_VARIANT)
        {
            tickOffsetVariant = 0;
        }
    }

    /**
     * Construct a target.
     * TODO: Remove once all Targets transitioned to tickRate
     * @param predicate the predicate for execution
     * @param state     the state to switch to
     */
    public AITarget(@NotNull final BooleanSupplier predicate, @Nullable final AIState state, final boolean isOkayToEat)
    {
        this(null, isOkayToEat, predicate, () -> state, 1);
    }

    /**
     * Construct a target.
     *
     * @param predicate the predicate for execution
     * @param state     the state to switch to
     */
    public AITarget(@NotNull final BooleanSupplier predicate, @Nullable final AIState state, final boolean isOkayToEat, @NotNull final int tickRate)
    {
        this(null, isOkayToEat, predicate, () -> state, tickRate);
    }

    /**
     * Construct a target.
     * TODO: Remove once all Targets transitioned to tickRate
     * @param predicateState the state it needs to be | null
     * @param state          the state to switch to
     */
    public AITarget(@NotNull final AIState predicateState, @Nullable final AIState state, final boolean isOkayToEat)
    {
        this(predicateState, isOkayToEat, () -> state, 1);
    }

    /**
     * Construct a target.
     *
     * @param predicateState the state it needs to be | null
     * @param state          the state to switch to
     */
    public AITarget(@NotNull final AIState predicateState, @Nullable final AIState state, final boolean isOkayToEat, @NotNull final int tickRate)
    {
        this(predicateState, isOkayToEat, () -> state, tickRate);
    }

    /**
     * Construct a target.
     * TODO: Remove once all Targets transitioned to tickRate
     * @param state  the state it needs to be | null
     * @param action the action to apply
     */
    public AITarget(@Nullable final AIState state, final boolean isOkayToEat, @NotNull final Supplier<AIState> action)
    {
        this(state, isOkayToEat, () -> true, action, 1);
    }

    /**
     * Construct a target.
     *
     * @param state  the state it needs to be | null
     * @param action the action to apply
     */
    public AITarget(@Nullable final AIState state, final boolean isOkayToEat, @NotNull final Supplier<AIState> action, @NotNull final int tickRate)
    {
        this(state, isOkayToEat, () -> true, action, tickRate);
    }

    /**
     * The state this target matches on.
     * Use null to match on all states.
     *
     * @return the state
     */
    @Nullable
    public AIState getState()
    {
        return state;
    }

    /**
     * Return whether the ai wants this target to be executed.
     *
     * @return true if execution is wanted.
     */
    public boolean test()
    {
        return predicate.getAsBoolean();
    }

    /**
     * Execute this target.
     * Do some stuff and return the state transition.
     *
     * @return the new state the ai is in. null if no change.
     */
    public AIState apply()
    {
        return action.get();
    }

    /**
     * Called to see if it is okay for the citizen to eat when
     * in this state.
     *
     * @return indicates if it is Okay to eat in this state
     */
    public boolean isOkayToEat()
    {
        return okayToEat;
    }

    /**
     * Never unregister persistent AITargets
     *
     * @return false
     */
    public boolean shouldUnregister()
    {
        return false;
    }

    /**
     * Returns the intended tickRate of the AITarget
     *
     * @return Tickrate
     */
    public int getTickRate()
    {
        return tickRate;
    }

    /**
     * Allow to dynamically change the tickrate
     *
     * @param tickRate rate at which the AITarget should tick
     */
    public void setTickRate(@NotNull final int tickRate)
    {
        this.tickRate = tickRate;
    }

    /**
     * Returns a random offset to Ticks
     *
     * @return random
     */
    public int getTickOffset()
    {
        return tickOffset;
    }
}
