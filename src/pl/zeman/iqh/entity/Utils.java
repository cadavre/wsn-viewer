
package pl.zeman.iqh.entity;

import java.util.ArrayList;

public class Utils {

    public static Node getNode(ArrayList<Node> nodes, int id) {

        for (Node node : nodes) {
            if (node.getId() == id) {
                return node;
            }
        }

        return null;
    }

    public static Result getResultForNode(ArrayList<Result> results, Node node) {

        return getResultForID(results, node.getId());
    }

    public static Health getHealthForNode(ArrayList<Health> healths, Node node) {

        return getHealthForID(healths, node.getId());
    }

    public static Result getResultForID(ArrayList<Result> results, int id) {

        for (Result result : results) {
            if (result.getNodeId() == id) {
                return result;
            }
        }

        return null;
    }

    public static Health getHealthForID(ArrayList<Health> healths, int id) {

        for (Health health : healths) {
            if (health.getNodeId() == id) {
                return health;
            }
        }

        return null;
    }
}
