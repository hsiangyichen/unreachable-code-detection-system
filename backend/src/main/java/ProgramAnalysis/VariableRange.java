package ProgramAnalysis;

import org.yaml.snakeyaml.util.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;

public class VariableRange {
    public int lowerBound;
    public int upperBound;
    public boolean infLowerBound;
    public boolean infUpperBound;

    public void plus( VariableRange a, VariableRange b ) {
        if( a.infUpperBound || b.infUpperBound )
            this.infUpperBound = true;
        if( a.infLowerBound || b.infLowerBound )
            this.infLowerBound = true;
        this.lowerBound = a.lowerBound + b.lowerBound;
        this.upperBound = a.upperBound + b.upperBound;
    }

    public void minus( VariableRange a, VariableRange b ) {
        if( a.infUpperBound || b.infLowerBound )
            this.infUpperBound = true;
        if( b.infLowerBound || b.infUpperBound )
            this.infLowerBound = true;
        this.lowerBound = a.lowerBound - b.upperBound;
        this.upperBound = a.upperBound - b.lowerBound;
    }

    public void mult( VariableRange a, VariableRange b ) {
        if( a.equals( 0 ) || b.equals( 0 ) ) {
            this.lowerBound = 0;
            this.upperBound = 0;
            return;
        }

        // check for infUpperBound
        if( a.infUpperBound && ( b.infUpperBound || b.lowerBound > 0 || b.upperBound > 0 ) )
            this.infUpperBound = true;
        else if( b.infUpperBound && ( a.lowerBound > 0 || a.upperBound > 0 ) )
            this.infUpperBound = true;
        else if( a.infLowerBound && ( b.infLowerBound || b.lowerBound < 0 || b.upperBound < 0 ) )
            this.infUpperBound = true;
        else if( b.infLowerBound && ( a.lowerBound < 0 || a.upperBound < 0 ) )
            this.infUpperBound = true;

        //check for infLowerBound
        if( a.infUpperBound && ( b.infLowerBound || b.lowerBound < 0 || b.upperBound < 0 ) )
            this.infLowerBound = true;
        else if( b.infUpperBound && ( a.lowerBound < 0 || a.upperBound < 0 ) )
            this.infLowerBound = true;
        else if( a.infLowerBound && ( b.infUpperBound || b.lowerBound > 0 || b.upperBound > 0 ) )
            this.infLowerBound = true;
        else if( b.infLowerBound && ( a.lowerBound > 0 || a.upperBound > 0 ) )
            this.infLowerBound = true;

        //calculate if we can get a bounded range
        Integer[] possibleBounds = { a.upperBound * b.upperBound, a.upperBound * b.lowerBound, a.lowerBound * b.upperBound, a.lowerBound * b.lowerBound };
        this.upperBound = Collections.max( Arrays.asList(possibleBounds) );
        this.lowerBound = Collections.min( Arrays.asList(possibleBounds) );
    }

    // Assumes that there is no division by 0
    public void div( VariableRange a, VariableRange b ) {
        if( b.equals( 0 ) ) {
            this.infLowerBound = true;
            this.infUpperBound = true;
            return;
        }

        if( ( b.contains( 0 ) && b.contains( 1 ) ) && ( a.upperBound > 0 || a.infUpperBound ) )
            this.infUpperBound = true;
        if( ( b.contains( 0 ) && b.contains( 1 ) ) && ( a.lowerBound < 0 || a.infLowerBound ) )
            this.infLowerBound = true;
        if( ( b.contains( 0 ) && b.contains( -1 ) ) && ( a.upperBound > 0 || a.infUpperBound ) )
            this.infLowerBound = true;
        if( ( b.contains( 0 ) && b.contains( -1 ) ) && ( a.lowerBound < 0 || a.infLowerBound ) )
            this.infUpperBound = true;

        int effectiveBLower = b.lowerBound == 0? b.lowerBound+1 : b.lowerBound;
        int effectiveBUpper = b.upperBound == 0? b.upperBound-1 : b.upperBound;
        Integer[] possibleBounds = { a.upperBound / effectiveBUpper, a.upperBound / effectiveBLower, a.lowerBound / effectiveBUpper, a.lowerBound / effectiveBLower };
        this.upperBound = Collections.max( Arrays.asList( possibleBounds ) );
        this.lowerBound = Collections.min( Arrays.asList( possibleBounds ) );
    }

    public boolean equals( Integer val ) {
        if( this.infLowerBound || this.infUpperBound )
            return false;
        if( this.lowerBound == val && this.upperBound == val )
            return true;
        return false;
    }

    public boolean equals( VariableRange range ) {
        if( this.infUpperBound != range.infUpperBound || this.infLowerBound != range.infLowerBound )
            return false;
        return ( this.infLowerBound || this.lowerBound == range.lowerBound ) && ( this.infUpperBound || this.upperBound == range.upperBound );
    }

    // range < val
    public boolean lt( Integer val ) {
        if( this.infLowerBound || this.upperBound < val )
            return true;
        return false;
    }

    // this < range
    public boolean lt( VariableRange range ) {
        return !this.infUpperBound && !range.infLowerBound && this.upperBound < range.lowerBound;
    }

    public boolean gt( Integer val ) {
        return this.infUpperBound || this.lowerBound > val;
    }

    // this > range
    public boolean gt( VariableRange range ) {
        return !this.infLowerBound && !range.infUpperBound && this.lowerBound > range.upperBound;
    }

    public boolean le( Integer val ) {
        return !this.gt(val);
    }

    // this <= range
    public boolean le( VariableRange range ) {
        return !this.infUpperBound && !range.infLowerBound && this.upperBound <= range.lowerBound;
    }

    public boolean ge( Integer val ) {
        return !this.lt( val );
    }

    // this >= range
    public boolean ge( VariableRange range ) {
        return !this.infLowerBound && !range.infUpperBound && this.lowerBound >= range.upperBound;
    }

    public boolean contains( Integer val ) {
        if( ( this.infLowerBound || this.lowerBound <= val ) && ( this.infUpperBound || this.upperBound >= val ) )
            return true;
        return false;
    }

    @Override
    public Object clone() {
        try{
            VariableRange copyVarRange = (VariableRange) super.clone();
            return copyVarRange;
        } catch ( CloneNotSupportedException e ) {
            VariableRange copyVarRange = new VariableRange();
            copyVarRange.upperBound = this.upperBound;
            copyVarRange.lowerBound = this.lowerBound;
            copyVarRange.infUpperBound = this.infUpperBound;
            copyVarRange.infLowerBound = this.infLowerBound;
            return copyVarRange;
        }
    }

    public VariableRange fromInt( int n ) {
        this.lowerBound = n;
        this.upperBound = n;
        this.infLowerBound = false;
        this.infUpperBound = false;
        return this;
    }

    public void unionRange( VariableRange other ) {
        this.upperBound = Math.max( this.upperBound, other.upperBound );
        this.lowerBound = Math.min( this.lowerBound, other.lowerBound );
        this.infUpperBound = this.infUpperBound || other.infUpperBound;
        this.infLowerBound = this.infLowerBound || other.infLowerBound;
    }
}
