package dev.ghen.thirst.foundation.gui.appleskin;

public class ThirstValues
{
    /**
     * This is some of the most esoteric garbage code you'll ever see. Read at your own risk
     * also this is adapted from AppleSkin
     * */
    public final int thirst;
    public final float quenchedModifier;

    public ThirstValues(int thirst, float saturationModifier)
    {
        this.thirst = thirst;
        this.quenchedModifier = saturationModifier;
    }

    public float getQuenchedIncrement()
    {
        return thirst * quenchedModifier * 2f;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof ThirstValues that)) return false;

        return thirst == that.thirst && Float.compare(that.quenchedModifier, quenchedModifier) == 0;
    }

    @Override
    public int hashCode()
    {
        int result = thirst;
        result = 31 * result + (quenchedModifier != +0.0f ? Float.floatToIntBits(quenchedModifier) : 0);
        return result;
    }
}
