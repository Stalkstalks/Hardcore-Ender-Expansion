package chylex.hee.world.structure.island.biome.feature.forest.ravageddungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import chylex.hee.world.structure.island.biome.feature.forest.ravageddungeon.DungeonElementType.RoomCombo;
import gnu.trove.set.hash.TIntHashSet;

public class RavagedDungeonGenerator {

    private final Random rand;
    private final byte width, height;
    private final List<TIntHashSet> blockedLocs = new ArrayList<>(4);
    public final DungeonLayer[] layers;

    public RavagedDungeonGenerator(int width, int height, int layers, Random rand) {
        this.rand = rand;
        this.width = (byte) width;
        this.height = (byte) height;
        this.layers = new DungeonLayer[layers];

        for (int a = 0; a < layers; a++) blockedLocs.add(new TIntHashSet());
    }

    public void blockLocation(int layer, int x, int y) {
        blockedLocs.get(layer).add(x + y * width);
    }

    public boolean generate() {
        DungeonLayer dung;
        boolean hasSmallRoom, hasBlockRoom;
        int entranceX = -1, entranceY = -1;
        int attemptLayer = 0;

        for (int layer = 0; layer < layers.length; layer++) {
            while (++attemptLayer < 80) {
                hasSmallRoom = hasBlockRoom = false;

                dung = new DungeonLayer(width, height, blockedLocs.get(layer));

                if (layer == 0) dung.createEntrance(rand.nextInt(width), rand.nextInt(height));
                else dung.createDescendBottom(entranceX, entranceY);

                switch (layer) {
                    case 0:
                        RoomCombo.setRoomWeights(25, 5, 0, 0);
                        break;
                    case 1:
                        RoomCombo.setRoomWeights(25, 8, 2, 1);
                        break;
                    case 2:
                        RoomCombo.setRoomWeights(25, 9, 2, 3);
                        break;
                    default:
                }

                DungeonElementList elements = dung.getElements();

                int iteration, attempt, attempts, path, pathLength, test;
                DungeonDir dir, nextDir;

                for (iteration = 0; iteration < 1 + Math.min(rand.nextInt(5), 3); iteration++) {
                    attempts = 18 + rand.nextInt(10);

                    for (attempt = 0; attempt < attempts; attempt++) {
                        test = 0;
                        dir = DungeonDir.random(rand);

                        while (true) {
                            pathLength = rand.nextInt(3) + rand.nextInt(2) * rand.nextInt(2 + rand.nextInt(3));

                            for (path = 0; path < pathLength; path++) {
                                if (!dung.addHallway(dir)) break;
                                else test = 0;
                            }

                            nextDir = test > 0 ? DungeonDir.random(rand)
                                    : (rand.nextBoolean() ? dir.rotatedLeft() : dir.rotatedRight());

                            if (rand.nextFloat() > 0.78F && dung.canGenerateRoom(dir)) {
                                int blocks = dung.addRoom(dir, rand);

                                if (blocks > 0) {
                                    if (blocks == 1) hasSmallRoom = true;
                                    else if (blocks == 4) hasBlockRoom = true;

                                    test = 0;

                                    if (rand.nextInt(4) == 0) break;
                                    else {
                                        nextDir = DungeonDir.random(rand);
                                        if (nextDir == dir.reversed()) nextDir = DungeonDir.random(rand); // two
                                                                                                          // attempts
                                    }
                                }
                            }

                            dir = nextDir;
                            if (++test > 4) break;
                        }

                        DungeonElement element = elements.getRandom(
                                rand.nextFloat() < 0.22F ? DungeonElementType.ROOM : DungeonElementType.HALLWAY,
                                rand);
                        if (element != null) dung.moveToElement(element);
                        else break;
                    }

                    dung.moveToEntrance();
                }

                DungeonElement hallway1, hallway2;

                for (attempt = 0; attempt < 15; attempt++) {
                    hallway1 = elements.getRandom(DungeonElementType.HALLWAY, rand);
                    if (hallway1 == null) break;

                    for (int dirAttempt = 0; dirAttempt < 4; dirAttempt++) {
                        DungeonDir checkDir = DungeonDir.values[rand.nextInt(DungeonDir.values.length)];
                        hallway2 = elements.getAt(hallway1.x + checkDir.addX, hallway1.y + checkDir.addY);

                        if (hallway2 != null) {
                            hallway1.connect(checkDir);
                            hallway2.connect(checkDir.reversed());
                            break;
                        }
                    }
                }

                List<DungeonElement> rooms = elements.getAll(DungeonElementType.ROOM);
                if (rooms.size() < 7 || (layer < layers.length - 1 && !hasSmallRoom)
                        || (layer == layers.length - 1 && !hasBlockRoom))
                    continue;

                while (true) {
                    DungeonElement room = rooms.get(rand.nextInt(rooms.size()));
                    int groupSize = elements.getGrouped(room).size();

                    if (layer < layers.length - 1) {
                        if (groupSize != 1) continue;
                        entranceX = room.x;
                        entranceY = room.y;
                        dung.createDescend(room.x, room.y);
                    } else if (layer == layers.length - 1) {
                        if (groupSize != 4) continue;
                        dung.createEnd(room.x, room.y);
                    }

                    layers[layer] = dung;
                    break;
                }

                break;
            }

            if (attemptLayer >= 80) return false;
        }

        return true;
    }
}
