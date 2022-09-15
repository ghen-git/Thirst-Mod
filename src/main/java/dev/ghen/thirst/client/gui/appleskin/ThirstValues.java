package dev.ghen.thirst.client.gui.appleskin;

public class ThirstValues
{
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
        if (!(o instanceof ThirstValues)) return false;

        ThirstValues that = (ThirstValues) o;

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
