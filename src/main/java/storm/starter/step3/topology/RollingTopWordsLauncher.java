package storm.starter.step3.topology;

import static storm.starter.utils.ClusterHelper.aClusterWith;
import storm.starter.utils.TopologyBuilder;
import backtype.storm.testing.TestWordSpout;
import backtype.storm.topology.IBasicBolt;
import backtype.storm.topology.IRichSpout;

public class RollingTopWordsLauncher {
	private static final int NUM_BUCKETS = 60;
	private static final int TRACK_MINUTES = 10;
	private static final int TOP_N = 3;
	private static final int MILLISECONDS_OFFEST = 2000;
	private static final String SPOUT_FIELD = "word";

	public static void main(final String[] args) throws Exception {
		final TopologyBuilder topology = new TopologyBuilder();

		final IRichSpout testWordSpout = new TestWordSpout();
		final RollingCountObjects rollingCountObjects = new RollingCountObjects(NUM_BUCKETS, TRACK_MINUTES);
		final IBasicBolt rankObjects = new RankObjects(TOP_N, MILLISECONDS_OFFEST);
		final IBasicBolt mergeObjects = new MergeObjects(TOP_N, MILLISECONDS_OFFEST);

		topology.setSpout(testWordSpout, 5);
		topology.setBolt(rollingCountObjects, 4).fieldsGrouping(testWordSpout, SPOUT_FIELD);
		topology.setBolt(rankObjects, 4).fieldsGrouping(rollingCountObjects, rollingCountObjects.getField());
		topology.setBolt(mergeObjects).globalGrouping(rankObjects);

		aClusterWith(topology).runAsLocal();
	}
}
