package ProgramAnalysis;

public class FlagTuple {
    public boolean returnFlag;
    public boolean breakFlag;

    public FlagTuple(boolean returnFlag, boolean breakFlag) {
        this.returnFlag = returnFlag;
        this.breakFlag = breakFlag;
    }

    public boolean getReturnFlag() {
        return this.returnFlag;
    }

    public boolean getBreakFlag() {
        return this.breakFlag;
    }

    // this && other
    public FlagTuple and(FlagTuple other ) {
        return new FlagTuple( this.returnFlag && other.returnFlag, this.breakFlag && other.breakFlag );
    }
}
