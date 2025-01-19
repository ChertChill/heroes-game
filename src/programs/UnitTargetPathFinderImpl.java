package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.*;

/**
 * Расчет и доказательство алгоритмической сложности метода getTargetPath:
 * -----------------------------------------------------------------------
 * 1. Определение области поиска:
 *    - Определяются min/max границы области на основе позиций юнитов с учетом заданного размера области.
 *    - Итого: O(1), поскольку расчет границ поля происходит с использованием базовых операций и сравнения..
 * 2. Создание матрицы занятых клеток:
 *    - Метод getOccupiedMatrix работает со сложностью — O(n), где n — количество юнитов в списке existingUnitList.
 *    - Для каждого юнита проводится проверка на активность — тогда его позиция помечается как занятая.
 *    - Итого: O(n), где n — количество юнитов в списке.
 * 3. Основной цикл поиска пути по A* и JPS:
 *    - Метод aStarWithJPS является решающим при расчете сложности.
 *    - Используется приоритетная очередь, которая будет содержать возможные узлы для обработки и организована по стоимости пути с учетом эвристики.
 *    - При каждом шаге извлекается узел с минимальной стоимостью и для него вычисляются соседние узлы, используя Jump Point Search.
 *    - JPS активно сокращает количество соседей, что снижает сложность по сравнению с обычным поиском в ширину.
 *    - Основной цикл выполняется до тех пор, пока не будет найден путь или пока очередь не опустеет.
 *    - Итого:
 *      * В худшем случае, метод aStarWithJPS будет обрабатывать каждый возможный узел. Это количество узлов зависит от площади поля, которая в нашем случае ограничена размерами WIDTH и HEIGHT. Размер поля = WIDTH * HEIGHT.
 *      * Поиск пути в стандартном A* без JPS — это O(W * H), где W — ширина, а H — высота поля.
 *      * Применение JPS сокращает количество исследуемых соседей, особенно при обработке длинных прямых путей, что снижает константу. Однако в худшем случае его сложность все равно будет зависеть от числа возможных узлов и связана с количеством возможных прямых, которые необходимо будет проверить для каждого узла, и аналогично оценить в O(W * H).
 * 4. Построение пути:
 *    - После нахождения пути происходит его восстановление путем обратного прохода через хеш-карту предшественников.
 *    - Итого: этот процесс осуществляет метод constructPath — просто обход цепочки узлов, и его сложность составляет O(P), где P — количество шагов в пути (обычно гораздо меньше числа возможных узлов).
 *
 * Общая сложность:
 * ----------------
 * - Определение области поиска — O(1).
 * - Получение матрицы занятых клеток — O(n).
 * - Поиск пути по A* и JPS — O(WIDTH * HEIGHT).
 * - Построение пути — O(P).
 *
 * Итого:
 * ------
 * В худшем случае общая сложность — O(W * H), так как поиск пути доминирует, и число клеток (W * H) обычно больше, чем количество юнитов (N) и шагов в пути (P).
 */

public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {

    private static final int WIDTH = 27;    // Ширина игрового поля
    private static final int HEIGHT = 21;   // Высота игрового поля
    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Верх, низ, лево, право
    // Четыре направления - вверх, вниз, влево, вправо

    /**
     * Метод определяет список юнитов, подходящих для атаки, для атакующего юнита одной из армий.
     * Исключает ненужные попытки найти кратчайший путь между юнитами, которые не могут атаковать друг друга.
     *
     * @param attackUnit Юнит, совершающий атаку.
     * @param targetUnit Юнит, на которого нацелен путь.
     * @param existingUnitList Список всех существующих юнитов, занятые клетки которых нельзя использовать.
     * @return Список юнитов (List<Edge>), подходящих для атаки, для юнита атакующей армии.
     */
    @Override
    public List<Edge> getTargetPath(Unit attackUnit, Unit targetUnit, List<Unit> existingUnitList) {
        int margin = 5; // Размер области вокруг начала и цели, увеличивающий область поиска.

        // Определение границ ограниченной области поиска.
        int minX = Math.max(0, Math.min(attackUnit.getxCoordinate(), targetUnit.getxCoordinate()) - margin);
        int minY = Math.max(0, Math.min(attackUnit.getyCoordinate(), targetUnit.getyCoordinate()) - margin);
        int maxX = Math.min(WIDTH - 1, Math.max(attackUnit.getxCoordinate(), targetUnit.getxCoordinate()) + margin);
        int maxY = Math.min(HEIGHT - 1, Math.max(attackUnit.getyCoordinate(), targetUnit.getyCoordinate()) + margin);

        // Создание матрицы занятых клеток.
        boolean[][] occupiedMatrix = getOccupiedMatrix(existingUnitList, attackUnit, targetUnit);

        // Вызов JPS + A* для поиска кратчайшего пути
        return aStarWithJPS(attackUnit, targetUnit, occupiedMatrix, minX, minY, maxX, maxY);
    }

    /**
     * Создание матрицы занятых клеток.
     *
     * @param existingUnitList Список всех юнитов.
     * @param attackUnit Атакующий юнит.
     * @param targetUnit Цель атаки.
     * @return Матрица занятых клеток (boolean[][]) - true, если клетка занята, false - свободна.
     */
    private boolean[][] getOccupiedMatrix(List<Unit> existingUnitList, Unit attackUnit, Unit targetUnit) {
        boolean[][] occupied = new boolean[WIDTH][HEIGHT];

        for (Unit unit : existingUnitList) {
            if (unit.isAlive() && unit != attackUnit && unit != targetUnit) {
                occupied[unit.getxCoordinate()][unit.getyCoordinate()] = true;
            }
        }

        return occupied;
    }

    /**
     * Реализация поиска пути с использованием A* и Jump Point Search.
     *
     * @param attackUnit Атакующий юнит.
     * @param targetUnit Цель атаки.
     * @param occupiedMatrix Матрица занятых клеток.
     * @param minX Минимальная граница X для ограниченной области.
     * @param minY Минимальная граница Y для ограниченной области.
     * @param maxX Максимальная граница X для ограниченной области.
     * @param maxY Максимальная граница Y для ограниченной области.
     * @return Кратчайший путь (List<Edge>).
     */
    private List<Edge> aStarWithJPS(Unit attackUnit, Unit targetUnit, boolean[][] occupiedMatrix,
                                    int minX, int minY, int maxX, int maxY) {
        Map<Node, Integer> gScores = new HashMap<>();   // Стоимость пути от начальной точки до текущей.
        Map<Node, Integer> fScores = new HashMap<>();   // Прогноз на основе стоимости и эвристики.
        Map<Node, Node> previous = new HashMap<>();     // Для восстановления пути.
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(fScores::get));
        // Очередь узлов с приоритетом по наименьшему fScore.

        Node startNode = new Node(attackUnit.getxCoordinate(), attackUnit.getyCoordinate());
        Node targetNode = new Node(targetUnit.getxCoordinate(), targetUnit.getyCoordinate());

        // Начальные значения для начальной точки.
        gScores.put(startNode, 0);
        fScores.put(startNode, heuristic(startNode, targetNode));
        openSet.add(startNode);

        // Основной цикл A* поиска.
        while (!openSet.isEmpty()) {
            // Извлекаем узел с наименьшим fScore.
            Node current = openSet.poll();

            // Если достигли цели, возвращаем путь.
            if (current.equals(targetNode)) {
                return constructPath(previous, targetNode);
            }

            // Процесс поиска соседей с использованием Jump Point Search.
            for (Node neighbor : getJumpNeighbors(current, targetNode, occupiedMatrix, minX, minY, maxX, maxY)) {
                int tentativeGScore = gScores.getOrDefault(current, Integer.MAX_VALUE) + 1;

                if (tentativeGScore < gScores.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    gScores.put(neighbor, tentativeGScore);
                    fScores.put(neighbor, tentativeGScore + heuristic(neighbor, targetNode));
                    previous.put(neighbor, current);

                    // Добавляем в очередь, если узел еще не обработан.
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        // Если путь не найден, возвращаем пустой список.
        return Collections.emptyList();
    }

    /**
     * Расчет эвристики для оценки расстояния до цели.
     * Используется манхэттенское расстояние.
     *
     * @param node - Node: текущий узел.
     * @param target - Node: конечный узел.
     * @return int - эвристическое значение.
     */
    private int heuristic(Node node, Node target) {
        return Math.abs(node.getX() - target.getX()) + Math.abs(node.getY() - target.getY());
    }

    /**
     * Восстановление пути из предшественников.
     *
     * @param previous - Map<Node, Node>: мапа предшественников для восстановления пути.
     * @param target - Node: целевой узел.
     * @return List<Edge> - список точек пути от начальной точки до конечной.
     */
    private List<Edge> constructPath(Map<Node, Node> previous, Node target) {
        List<Edge> path = new ArrayList<>();
        Node current = target;

        // Восстановление пути, начиная с целевого узла.
        while (current != null) {
            // Добавляем текущий узел в путь.
            path.add(new Edge(current.getX(), current.getY()));

            // Переходим к предыдущему узлу.
            current = previous.get(current);
        }

        // Переворачиваем путь, так как он восстанавливается от конца.
        Collections.reverse(path);
        return path;
    }

    /**
     * Поиск соседей с использованием Jump Point Search.
     *
     * @param current Текущий узел.
     * @param target Целевой узел.
     * @param occupiedMatrix Матрица занятых клеток.
     * @param minX Минимальная граница по оси X.
     * @param minY Минимальная граница по оси Y.
     * @param maxX Максимальная граница по оси X.
     * @param maxY Максимальная граница по оси Y.
     * @return Список соседей (List<Node>), которые можно обработать.
     */
    private List<Node> getJumpNeighbors(Node current, Node target, boolean[][] occupiedMatrix,
                                        int minX, int minY, int maxX, int maxY) {
        List<Node> neighbors = new ArrayList<>();   // Список соседей, найденных с использованием JPS.

        // Проверка на наличие соседей в каждой из 4 направлений.
        for (int[] dir : DIRECTIONS) {
            Node jumpNode = jump(current.getX(), current.getY(), dir[0], dir[1], target, occupiedMatrix, minX, minY, maxX, maxY);

            // Добавляем соседей в список.
            if (jumpNode != null) {
                neighbors.add(jumpNode);
            }
        }

        return neighbors;
    }

    /**
     * Метод, реализующий Jump Point Search для поиска точки-прыжка на пути.
     * Использует рекурсию для перехода через валидные клетки и поиска точек, которые могут быть посещены.
     *
     * @param x Текущая координата по оси X.
     * @param y Текущая координата по оси Y.
     * @param dx Изменение по оси X (направление движения по оси X).
     * @param dy Изменение по оси Y (направление движения по оси Y).
     * @param target Цель поиска, до которой нужно дойти.
     * @param occupiedMatrix Матрица занятых клеток.
     * @param minX, minY Минимальные границы области поиска по осям X и Y.
     * @param maxX, maxY Максимальные границы области поиска по осям X и Y.
     * @return Точка (Node), до которой можно дойти, или null, если путь недоступен.
     */
    private Node jump(int x, int y, int dx, int dy, Node target, boolean[][] occupiedMatrix,
                      int minX, int minY, int maxX, int maxY) {
        int nx = x + dx;
        int ny = y + dy;

        // Проверка, находится ли координата в допустимом диапазоне.
        if (!isValid(nx, ny, occupiedMatrix, minX, minY, maxX, maxY)) return null;

        // Если цель найдена, возвращаем текущий узел.
        if (nx == target.getX() && ny == target.getY()) {
            return new Node(nx, ny);
        }

        // Проверка на необходимость "принудительного" прыжка.
        if (dx != 0 && dy == 0 && (isForced(nx, ny - 1, occupiedMatrix, minX, minY, maxX, maxY) ||
                isForced(nx, ny + 1, occupiedMatrix, minX, minY, maxX, maxY)) ||
                dy != 0 && dx == 0 && (isForced(nx - 1, ny, occupiedMatrix, minX, minY, maxX, maxY) ||
                        isForced(nx + 1, ny, occupiedMatrix, minX, minY, maxX, maxY))) {
            return new Node(nx, ny);
        }

        // Рекурсивный вызов для продолжения поиска пути.
        return jump(nx, ny, dx, dy, target, occupiedMatrix, minX, minY, maxX, maxY);
    }

    /**
     * Метод для проверки, является ли клетка "принудительной" для прыжка.
     * Есть ли на клетке присутствуют блокирующие элементы, которые могут требовать обязательный переход.
     *
     * @param x Координата по оси X.
     * @param y Координата по оси Y.
     * @param occupiedMatrix Матрица занятых клеток.
     * @param minX, minY Минимальные границы области поиска.
     * @param maxX, maxY Максимальные границы области поиска.
     * @return true, если клетка принудительная, false в противном случае.
     */
    private boolean isForced(int x, int y, boolean[][] occupiedMatrix,
                             int minX, int minY, int maxX, int maxY) {
        // Проверка, что клетка является валидной и занята.
        return isValid(x, y, occupiedMatrix, minX, minY, maxX, maxY);
    }

    /**
     * Метод проверяет, является ли клетка допустимой для посещения.
     * Клетка считается валидной, если она находится в пределах границ карты и не занята.
     *
     * @param x Координата клетки по оси X.
     * @param y Координата клетки по оси Y.
     * @param occupiedMatrix Матрица занятых клеток.
     * @param minX, minY Минимальные границы области поиска.
     * @param maxX, maxY Максимальные границы области поиска.
     * @return true, если клетка является валидной, false в противном случае.
     */
    private boolean isValid(int x, int y, boolean[][] occupiedMatrix,
                            int minX, int minY, int maxX, int maxY) {
        // Проверка на выход за пределы карты и занятость клетки.
        return x >= minX && x <= maxX && y >= minY && y <= maxY && !occupiedMatrix[x][y];
    }

    /**
     * Класс, представляющий координаты клетки на поле.
     * Служит для представления узлов в алгоритмах поиска пути, таких как A* или JPS.
     */
    private static class Node {
        private final int x;
        private final int y;

        /**
         * Конструктор для создания нового узла.
         *
         * @param x Координата по оси X.
         * @param y Координата по оси Y.
         */
        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Геттер, возвращающий координату по оси X.
         *
         * @return координата по оси X.
         */
        public int getX() {
            return x;
        }

        /**
         * Геттер, возвращающий координату по оси Y.
         *
         * @return координата по оси Y.
         */
        public int getY() {
            return y;
        }

        /**
         * Переопределенный метод для сравнения двух объектов Node.
         * Используется для проверки, равны ли два узла по координатам (x, y).
         * Это важно для работы с коллекциями, такими как HashSet или HashMap, где необходимо правильно определять равенство объектов.
         *
         * @param o Объект для сравнения с текущим экземпляром Node.
         * @return true, если объекты равны по координатам, false в противном случае.
         */
        @Override
        public boolean equals(Object o) {
            // Если сравниваем тот же объект, возвращаем true.
            if (this == o) return true;

            // Проверяем, что объект o не null и является экземпляром Node.
            if (o == null || getClass() != o.getClass()) return false;

            // Приводим объект o к типу Node и сравниваем координаты x и y двух узлов.
            Node node = (Node) o;
            return x == node.x && y == node.y;
        }

        /**
         * Переопределенный метод для вычисления хеш-кода узла.
         * Используется для эффективного поиска и использования узлов в коллекциях, таких как HashMap или HashSet.
         *
         * @return Хеш-код для узла, основанный на его координатах.
         */
        @Override
        public int hashCode() {
            // Генерация хеш-кода, основанного на значениях координат x и y.
            return Objects.hash(x, y);
        }
    }
}
