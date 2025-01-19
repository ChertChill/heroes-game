package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.*;

/**
 * Расчет и доказательство алгоритмической сложности метода getSuitableUnits:
 * --------------------------------------------------------------------------
 * 1. Основной цикл по рядам юнитов:
 *    - Выполняется итерация по m рядам.
 *    - Для каждого ряда вызывается findLeftMostAliveUnit/findRightMostAliveUnit со сложностью O(n).
 *    - Итого: сложность цикла — O(m * n).
 * 2. Определение списка юнитов, подходящих для атаки:
 *    - В методах findLeftMostAliveUnit/findRightMostAliveUnit цикл проходит по каждому юниту в ряду (всего n) со сложностью — O(n).
 *    - Условие и операции внутри цикла имеют константную сложность — O(1).
 *    - Итого: сложность методов для каждого ряда — O(n).
 *
 * Общая сложность:
 * ----------------
 * - Основной цикл — O(m * n).
 * - Определение списка юнитов, подходящих для атаки — O(n).
 *
 * Итого:
 * ------
 * Метод getSuitableUnits вызывает один из методов findLeftMostAliveUnit/findRightMostAliveUnit для каждого ряда (всего m).
 * Таким образом, общая сложность — O(m * n), но при фиксированном m (равно 3), фактическая сложность — O(n), где n — количество юнитов в одном ряду.
 */


public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    /**
     * Метод определяет список юнитов, подходящих для атаки, для атакующего юнита одной из армий.
     * Исключает ненужные попытки найти кратчайший путь между юнитами, которые не могут атаковать друг друга.
     *
     * @param unitsByRow Список рядов юнитов противника (List<List<Unit>>).
     * @param isLeftArmyTarget Булевый флаг, указывающий, атакуется ли левая армия (армия компьютера).
     * @return Список подходящих юнитов (List<Unit>).
     */
    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow, boolean isLeftArmyTarget) {
        List<Unit> suitableUnits = new ArrayList<>();

        // Перебираем каждый ряд юнитов
        for (List<Unit> row : unitsByRow) {
            // Определяем подходящего юнита в зависимости от цели атаки
            Unit targetUnit = isLeftArmyTarget
                    ? findLeftMostAliveUnit(row)
                    : findRightMostAliveUnit(row);

            // Добавляем подходящего юнита в список, если он найден
            if (targetUnit != null) {
                suitableUnits.add(targetUnit);
            }
        }

        return suitableUnits;
    }

    /**
     * Находит живого юнита с минимальной координатой Y в ряду.
     *
     * @param row Список юнитов в одном ряду (List<Unit>), которые могут быть живыми или мертвыми.
     * @return Юнит с минимальной координатой Y среди живых юнитов (Unit) или null, если таких юнитов нет.
     */
    private Unit findLeftMostAliveUnit(List<Unit> row) {
        Unit leftMostUnit = null;

        // Ищем юнита с минимальной координатой Y
        for (Unit unit : row) {
            if (unit.isAlive() && (leftMostUnit == null || unit.getyCoordinate() < leftMostUnit.getyCoordinate())) {
                leftMostUnit = unit;
            }
        }

        return leftMostUnit;
    }

    /**
     * Находит живого юнита с максимальной координатой Y в ряду.
     *
     * @param row Список юнитов в одном ряду (List<Unit>), которые могут быть живыми или мертвыми.
     * @return Юнит с максимальной координатой Y среди живых юнитов (Unit) или null, если таких юнитов нет.
     */
    private Unit findRightMostAliveUnit(List<Unit> row) {
        Unit rightMostUnit = null;

        // Ищем юнита с максимальной координатой Y
        for (Unit unit : row) {
            if (unit.isAlive() && (rightMostUnit == null || unit.getyCoordinate() > rightMostUnit.getyCoordinate())) {
                rightMostUnit = unit;
            }
        }

        return rightMostUnit;
    }
}
