package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;

/**
 * Расчет и доказательство алгоритмической сложности метода generate:
 * -----------------------------------------------------------------
 * 1. Сортировка списка юнитов:
 *    - Метод sortUnitsByEfficiency сортирует unitList с помощью Comparator.
 *    - Сложность сортировки: O(n * log n), где n — количество юнитов в списке (размер списка unitList).
 * 2. Основной цикл по юнитам:
 *    - Данный цикл перебирает каждый юнит в списке unitList через метод addUnitsToArmy.
 *    - Его сложность — O(n), где n — размер unitList.
 * 3. Метод addUnitsToArmy:
 *    - Цикл внутри выполняется до MAX_UNIT_COUNT раз — O(MAX_UNIT_COUNT).
 *    - Каждая итерация вызывает:
 *      * generateUniqueCoordinate — в худшем случае координата генерируется за O(WIDTH * HEIGHT), хотя на практике он завершается быстрее благодаря равномерному распределению координат.
 *      * cloneUnitWithNewCoordinate — создает нового юнита (создание объекта и копирование нескольких карт бонусов) за O(k), где k — фиксированное число элементов бонусов (константная сложность — O(1)).
 *
 * Общая сложность:
 * ----------------
 * - Сортировка: O(n * log n).
 * - Основной цикл для каждого юнита (до n раз):
 *   * addUnitsToArmy — внутренний цикл выполняется O(MAX_UNIT_COUNT) раз.
 *   * generateUniqueCoordinate — O(WIDTH * HEIGHT), но практически меньше.
 *   * cloneUnitWithNewCoordinate — константная сложность, за O(1).
 *
 * Итого:
 * ------
 * O(n * log n) + O(n * MAX_UNIT_COUNT * (WIDTH * HEIGHT)), где MAX_UNIT_COUNT, WIDTH и HEIGHT являются константами.
 * Можно упростить до O(n * log n + n) -> O(n * log n).
 */

public class GeneratePresetImpl implements GeneratePreset {

    private static final int MAX_UNIT_COUNT = 11;   // Максимальное количество юнитов одного типа
    private static final int WIDTH = 3;             // Ширина игрового поля
    private static final int HEIGHT = 21;           // Высота игрового поля

    /**
     * Метод формирует пресет армии компьютера.
     *
     * @param unitList Список доступных юнитов для генерации.
     * @param maxPoints Максимально доступное количество очков для армии.
     * @return Army Сформированный пресет армии компьютера с набором юнитов и стоимостью.
     */
    @Override
    public Army generate(List<Unit> unitList, int maxPoints) {
        System.out.println("Начало формирования армии компьютера.");

        Army computerArmy = new Army();
        List<Unit> selectedUnits = new ArrayList<>();
        Set<String> occupiedCoordinates = new HashSet<>();

        // Сортировка юнитов по эффективности
        sortUnitsByEfficiency(unitList);

        int currentPoints = 0;  // Текущая стоимость армии

        // Основной цикл по списку доступных юнитов
        for (Unit unit : unitList) {
            currentPoints = addUnitsToArmy(
                    unit, selectedUnits, occupiedCoordinates, currentPoints, maxPoints
            );
        }

        // Устанавливаем список юнитов и очки в армию
        computerArmy.setUnits(selectedUnits);
        computerArmy.setPoints(currentPoints);
        System.out.println("Конец формирования армии компьютера.");
        return computerArmy;
    }

    /**
     * Сортирует список юнитов по их эффективности.
     * Эффективность определяется как сумма атаки и здоровья, делённая на стоимость.
     *
     * @param unitList Список юнитов для сортировки.
     */
    private void sortUnitsByEfficiency(List<Unit> unitList) {
        unitList.sort(Comparator.comparingDouble(
                unit -> -((double) (unit.getBaseAttack() + unit.getHealth()) / unit.getCost())));
    }

    /**
     * Добавляет юниты указанного типа в армию до достижения ограничений.
     *
     * @param unit Юнит, добавляемый в армию.
     * @param selectedUnits Список уже выбранных юнитов.
     * @param occupiedCoordinates Множество занятых координат.
     * @param currentPoints Текущая стоимость армии.
     * @param maxPoints Лимит доступных очков.
     * @return int Обновлённая стоимость армии после добавления юнитов.
     */
    private int addUnitsToArmy(Unit unit, List<Unit> selectedUnits, Set<String> occupiedCoordinates,
                               int currentPoints, int maxPoints) {
        Random random = new Random();
        int unitCount = 0;  // Счетчик добавленных юнитов данного типа

        // Пока не достигнут лимит юнитов или стоимость не превысила лимит очков
        while (unitCount < MAX_UNIT_COUNT && currentPoints + unit.getCost() <= maxPoints) {
            // Генерация уникальной координаты для юнита
            String coordinateKey = generateUniqueCoordinate(occupiedCoordinates, random);

            // Создание копии юнита с новой позицией
            Unit newUnit = cloneUnitWithNewCoordinate(unit, coordinateKey, unitCount);

            // Добавление юнита в список и обновление множества занятых координат
            selectedUnits.add(newUnit);
            occupiedCoordinates.add(coordinateKey);
            currentPoints += newUnit.getCost();
            unitCount++;

            System.out.println(newUnit.getName() + " добавлен в координаты: "
                    + newUnit.getxCoordinate() + ", " + newUnit.getyCoordinate());
        }

        return currentPoints;
    }

    /**
     * Генерирует уникальную координату на игровом поле.
     *
     * @param occupiedCoordinates Множество занятых координат.
     * @param random Генератор случайных чисел.
     * @return String Уникальная строка, представляющая координаты.
     */
    private String generateUniqueCoordinate(Set<String> occupiedCoordinates, Random random) {
        String coordinateKey;   // Ключ координаты в формате "x_y"
        do {
            // Генерация случайных координат в заданном диапазоне [0, значение)
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            coordinateKey = x + "_" + y;
        } while (occupiedCoordinates.contains(coordinateKey));  // Проверка уникальности
        return coordinateKey;
    }

    /**
     * Создаёт копию юнита с новыми координатами.
     *
     * @param unit Исходный юнит для копирования.
     * @param coordinateKey Ключ координат в формате "x_y".
     * @param unitIndex Индекс юнита данного типа.
     * @return Unit Копия юнита с заданными координатами.
     */
    private Unit cloneUnitWithNewCoordinate(Unit unit, String coordinateKey, int unitIndex) {
        // Разделение ключа координат на x и y
        String[] coords = coordinateKey.split("_");
        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);

        // Создание нового объекта юнита
        return new Unit(
                unit.getUnitType() + " " + unitIndex,
                unit.getUnitType(),
                unit.getHealth(),
                unit.getBaseAttack(),
                unit.getCost(),
                unit.getAttackType(),
                new HashMap<>(unit.getAttackBonuses()),
                new HashMap<>(unit.getDefenceBonuses()),
                x,
                y
        );
    }
}