package thesis.model.domain.components.constraints.utils;

import java.util.Objects;

public class Block {
    private int start;
    private int end;

    public Block(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int Length() {
        return end - start;
    }

    public int CompareTo(Block other)
    {
        return start - other.start;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Block)) return false;
        Block block = (Block) o;
        return start == block.start && end == block.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
