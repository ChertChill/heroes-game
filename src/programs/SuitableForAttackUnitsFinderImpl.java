package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.*;

public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow, boolean isLeftArmyTarget) {
        List<Unit> suitableUnits = new ArrayList<>();

        for (List<Unit> row : unitsByRow) {
            for (Unit unit : row) {
                if (unit != null && unit.isAlive()) {
                    // Check if the unit is not blocked
                    if (isLeftArmyTarget && isRightmostUnit(unit, row)) {
                        suitableUnits.add(unit);
                    } else if (!isLeftArmyTarget && isLeftmostUnit(unit, row)) {
                        suitableUnits.add(unit);
                    }
                }
            }
        }

        return suitableUnits;
    }

    private boolean isRightmostUnit(Unit unit, List<Unit> row) {
        // Check if the unit is the rightmost in its row
        int unitIndex = row.indexOf(unit);
        return row.subList(unitIndex + 1, row.size()).stream().allMatch(Objects::isNull);
    }

    private boolean isLeftmostUnit(Unit unit, List<Unit> row) {
        // Check if the unit is the leftmost in its row
        int unitIndex = row.indexOf(unit);
        return row.subList(0, unitIndex).stream().allMatch(Objects::isNull);
    }
}
