package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.*;
import java.util.stream.*;

/**
 * Расчет и доказательство алгоритмической сложности метода simulate:
 * ------------------------------------------------------------------
 * 1. Инициализация списка юнитов
 *    - Метод initActiveUnits фильтрует юнитов с помощью stream().filter() со сложностью — O(n), где n — количество юнитов в армии.
 *    - Сортировка по убыванию атаки с помощью sorted() со сложностью — O(n * log n).
 *    - Итого: для одной армии сложность этой операции будет — O(n log n), а для обеих армий — 2 * O(n log n) -> O(n log n).
 * 2. Основной цикл симуляции:
 *    - Данный цикл работает, пока есть живые юниты в обеих армиях.
 *    - В каждом раунде симуляции:
 *      * Перебираем каждого юнита для выполнения атаки — O(n), так как должны быть проверены все юниты в обеих армиях.
 *      * Выполняем атаку для каждого юнита — O(1), по условию.
 *    - Удаляем мертвых юнитов с помощью метода removeIf() со сложностью O(n), так как этот метод проверяет каждого юнита.
 *    - Итого: в одном раунде для обеих армий суммарная сложность — O(n), так как проходим по каждому юниту один раз и удаляем мертвых юнитов.
 * 3. Количество раундов:
 *    - Основной цикл продолжается до тех пор, пока хотя бы одна армия не останется без живых юнитов.
 *    - В худшем случае армия может потерять юнитов по одному за каждый раунд.
 *    - Итого: цикл может быть выполнен до n раз — O(n), где n — количество юнитов в армии.
 *
 * Общая сложность:
 * ----------------
 * - Инициализация — O(n * log n).
 * - Основной цикл — O(n) за каждый раунд. В худшем случае количество раундов — O(n).
 * - Удаление мертвых юнитов — O(n) за каждый раунд.
 *
 * Итого:
 * ------
 * Для n раундов сложность — O(n^2).
 * Учитывая инициализацию сложность — O(n * log n + n^2) -> O (n^2).
 */

public class SimulateBattleImpl implements SimulateBattle {
    private PrintBattleLog printBattleLog; // Позволяет логировать. Использовать после каждой атаки юнита

    /**
     * Метод осуществляет симуляцию боя между армией игрока и армией компьютера.
     * Проводит бой, следуя установленным правилам.
     *
     * @param playerArmy Армия игрока, содержащая список её юнитов.
     * @param computerArmy Армия компьютера, содержащая список её юнитов.
     * @throws InterruptedException В случае прерывания потока.
     */
    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
        // Инициализация списка живых юнитов с их сортировкой по убыванию атаки.
        List<Unit> playerUnits = initActiveUnits(playerArmy.getUnits());
        List<Unit> computerUnits = initActiveUnits(computerArmy.getUnits());

        // Пока в обеих армиях есть живые юниты
        while (!playerUnits.isEmpty() && !computerUnits.isEmpty()) {
            // Симуляция раунда
            simulateRound(playerUnits, computerUnits);

            // Удаляем мертвых юнитов из обеих армий
            // Проходим по всем юнитам, чтобы исключить мертвых.
            playerUnits.removeIf(unit -> !unit.isAlive());
            computerUnits.removeIf(unit -> !unit.isAlive());
        }
    }

    /**
     * Метод для инициализации списка живых юнитов.
     * Ожидается, что юниты будут отсортированы по убыванию их атакующего значения.
     *
     * @param units Список юнитов.
     * @return Отсортированный список живых юнитов.
     */
    private List<Unit> initActiveUnits(List<Unit> units) {
        // Отфильтровываем только живых юнитов и сортируем по убыванию атаки.
        return units.stream()
                .filter(Unit::isAlive)
                .sorted(Comparator.comparingInt(Unit::getBaseAttack).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Метод для симуляции одного раунда битвы.
     * Каждый юнит по очереди совершает атаку.
     *
     * @param playerUnits Список живых юнитов армии игрока.
     * @param computerUnits Список живых юнитов армии компьютера.
     * @throws InterruptedException В случае прерывания потока.
     */
    private void simulateRound(List<Unit> playerUnits, List<Unit> computerUnits) throws InterruptedException {
        // Собираем всех живых юнитов обеих армий.
        List<Unit> allUnits = new ArrayList<>();
        allUnits.addAll(playerUnits);
        allUnits.addAll(computerUnits);

        // Перебираем каждого юнита для выполнения атаки.
        for (Unit currentUnit : allUnits) {
            if (!currentUnit.isAlive()) continue;

            // Получаем цель для атаки.
            Unit target = currentUnit.getProgram().attack();

            // Выполняем атаку, если цель существует и жива.
            if (target != null && target.isAlive()) {
                executeAttack(currentUnit, target);
            }
        }
    }

    /**
     * Метод для выполнения атаки одного юнита по другому.
     *
     * @param attacker Атакующий юнит.
     * @param target Цель атаки.
     */
    private void executeAttack(Unit attacker, Unit target) {
        // Наносим урон цели, уменьшаем её здоровье на величину атаки атакующего.
        int damage = attacker.getBaseAttack();
        target.setHealth(target.getHealth() - damage);

        // Если здоровье цели стало 0 или меньше, юнит умирает.
        if (target.getHealth() <= 0) {
            target.setAlive(false);
        }

        // Логируем атаку для отображения в журнале.
        printBattleLog.printBattleLog(attacker, target);
    }
}