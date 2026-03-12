package edu.uob;

import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GameEntityParser {

    private final File entityFile;

    public GameEntityParser(File entityFile) {
        this.entityFile = entityFile;
    }

    public GameWorld parse() {
        GameLocation startLocation = null;
        GameLocation storeroom = null;
        Map<String, GameLocation> tempLocations = new HashMap<String, GameLocation>();
        Map<String, GameEntity> tempEntities = new HashMap<String, GameEntity>();

        try {
            Parser parser = new Parser();
            parser.parse(new FileReader(this.entityFile));
            List<Graph> graphs = parser.getGraphs();

            Graph topGraph = graphs.get(0);
            List<Graph> mainSubgraphs = topGraph.getSubgraphs();

            Graph locationsGraph = mainSubgraphs.get(0);
            List<Graph> locations = locationsGraph.getSubgraphs();

            for (int i = 0; i < locations.size(); i++) {
                Graph locationGraph = locations.get(i);
                Node locationNode = locationGraph.getNodes(false).get(0);
                String locName = locationNode.getId().getId();

                String locDescription = locationNode.getAttribute("description");
                GameLocation location = new GameLocation(locName, locDescription);
                tempLocations.put(locName.toLowerCase(), location);

                if (i == 0) {
                    startLocation = location;
                }
                if (locName.equalsIgnoreCase("storeroom")) {
                    storeroom = location;
                }

                List<Graph> entityGroups = locationGraph.getSubgraphs();
                for (int j = 0; j < entityGroups.size(); j++) {
                    Graph group = entityGroups.get(j);
                    String groupType = group.getId().getId();
                    List<Node> entities = group.getNodes(false);

                    for (int k = 0; k < entities.size(); k++) {
                        Node node = entities.get(k);
                        String name = node.getId().getId();
                        String description = node.getAttribute("description");

                        GameEntity entity = null;
                        if (groupType.equals("artefacts")) {
                            entity = new GameArtefact(name, description);
                        } else if (groupType.equals("furniture")) {
                            entity = new GameFurniture(name, description);
                        } else if (groupType.equals("characters")) {
                            entity = new GameCharacter(name, description);
                        }

                        if (entity != null) {
                            location.addEntity(entity);
                            tempEntities.put(name.toLowerCase(), entity);
                        }
                    }
                }
            }

            if (storeroom == null) {
                storeroom = new GameLocation("storeroom", "A hidden container for unused entities.");
                tempLocations.put("storeroom", storeroom);
            }

            GameWorld world = new GameWorld(startLocation, storeroom);
            for (Iterator<String> it = tempLocations.keySet().iterator(); it.hasNext();) {
                String key = it.next();
                GameLocation loc = tempLocations.get(key);
                world.addLocation(loc);
            }
            for (Iterator<String> it = tempEntities.keySet().iterator(); it.hasNext();) {
                String key = it.next();
                GameEntity ent = tempEntities.get(key);
                world.registerEntity(ent);
            }

            Graph pathsGraph = mainSubgraphs.get(1);
            List<Edge> edges = pathsGraph.getEdges();
            for (int i = 0; i < edges.size(); i++) {
                Edge edge = edges.get(i);
                String from = edge.getSource().getNode().getId().getId();
                String to = edge.getTarget().getNode().getId().getId();

                GameLocation fromLoc = world.getLocationByName(from);
                GameLocation toLoc = world.getLocationByName(to);

                if (fromLoc != null && toLoc != null) {
                    fromLoc.connectTo(toLoc);
                }
            }

            return world;

        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error parsing entity DOT file: ");
            sb.append(e.getMessage());
            System.err.println(sb.toString());
            return null;
        }
    }
}
