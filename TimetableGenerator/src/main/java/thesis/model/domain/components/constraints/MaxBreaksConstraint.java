package thesis.model.domain.components.constraints;

import thesis.model.domain.components.Constraint;
import thesis.model.domain.components.ScheduledLesson;
import thesis.model.domain.components.Timetable;
import thesis.model.domain.components.TimetableConfiguration;
import thesis.model.domain.components.constraints.utils.Block;

import java.util.ArrayList;
import java.util.List;

public class MaxBreaksConstraint extends Constraint {
    public MaxBreaksConstraint(String restrictionType, int param1, int param2, Integer penalty, boolean required, TimetableConfiguration timetableConfiguration) {
        super(restrictionType, penalty, required, param1, param2, timetableConfiguration);
    }

    // The authors of this method are Edon Gashi and Kadri Sylejmani
    // source: https://github.com/edongashi/itc-2019
    @Override
    protected void getConflictingClasses(Timetable solution, conflictAction action) {
        List<ScheduledLesson> scheduledClasses = this.getScheduledClasses(solution);
        final int R = getFirstParameter();
        final int S = getSecondParameter();
        List<Block> blocks = new ArrayList<>();
        List<Block> mergedBlocks = new ArrayList<>();
        int totalOverflows = 0;

        for(int w=0; w < getNrWeeks(); w++) {
            for(int d=0; d < getNrDays(); d++) {
                blocks.clear();
                mergedBlocks.clear();

                for (ScheduledLesson scheduledLesson : scheduledClasses) {
                    if ((scheduledLesson.getDays() & (1 << d)) == 0 ||
                        (scheduledLesson.getWeeks() & (1 << w)) == 0) {
                        continue;
                    }

                    blocks.add(new Block(scheduledLesson.getStartSlot(), scheduledLesson.getEndSlot()));
                }

                var count = blocks.size();
                if (count == 0) {
                    continue;
                }

                blocks.sort(Block::CompareTo);

                mergedBlocks.add(blocks.get(0));
                for (int i = 1; i < count; i++)
                {
                    Block top = mergedBlocks.get(0);
                    Block current = blocks.get(i);
                    int topEnd = top.getEnd() + S;
                    if (topEnd < current.getStart()) {
                        // No overlap.
                        mergedBlocks.add(current);
                    } else { // Overlap
                        // We need to expand range.
                        mergedBlocks.remove(mergedBlocks.size() - 1);
                        mergedBlocks.add(new Block(top.getStart(), Math.max(top.getEnd(), current.getEnd())));
                    }
                }

                if (mergedBlocks.size() > R + 1)
                {
                    totalOverflows += mergedBlocks.size() - R - 1;
                }
            }
        }

        // TODO: confirm it is correct
        if(totalOverflows > 0) {
            scheduledClasses.forEach((cls) -> action.apply(cls.getClassId()));
        }
    }
}
