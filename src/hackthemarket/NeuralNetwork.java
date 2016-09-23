package hackthemarket;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NeuralNetwork {

    private static final double BIAS_SIGNAL = 1.0f;

    private final Neuron[] inputs;

    private final Neuron[] outputs;

    private final Neuron[] biases;

    private final Map<Integer, Neuron> network;

    public NeuralNetwork(Genome genome) {
        List<Node> nodes = genome.nodes();
        List<Link> links = genome.links();

        List<Neuron> inputNeurons = Util.newList();
        List<Neuron> outputNeurons = Util.newList();
        List<Neuron> biasNeurons = Util.newList();
        network = Util.newMap();
        int numNodes = nodes.size();
        for (int i = 0; i < numNodes; i++) {
            Node node = nodes.get(i);
            Neuron neuron = new Neuron(node.key(), node.threshold());
            network.put(node.key(), neuron);
            Allele allele = node.allele();
            if (allele.equals(Allele.Input)) {
                inputNeurons.add(neuron);
            } else if (allele.equals(Allele.Output)) {
                outputNeurons.add(neuron);
            } else if (allele.equals(Allele.Bias)) {
                biasNeurons.add(neuron);
            }
        }
        inputs = inputNeurons.toArray(new Neuron[]{});
        outputs = outputNeurons.toArray(new Neuron[]{});
        biases = biasNeurons.toArray(new Neuron[]{});

        for (Link link : links) {
            if (link.isEnabled()) {
                Neuron output = network.get(link.out());
                if (output == null) {
                    int key = link.out();
                    Node node = new Node(key, Allele.Hidden);
                    nodes.add(node);
                    output = new Neuron(key, node.threshold());
                    network.put(key, output);
                }
                Neuron input = network.get(link.in());
                if (input == null) {
                    int key = link.in();
                    Node node = new Node(key, Allele.Hidden);
                    nodes.add(node);
                    input = new Neuron(key, node.threshold());
                    network.put(key, input);
                }
                double weight = link.weight();
                Connection conn = new Connection(output, weight);
                input.attach(conn);
            }
        }
        Collections.sort(nodes);
        Collections.sort(links);
    }

    public void prime(double p) {
        int P = network.size();
        double[] prime = {p};
        for (int i = 0; i < P; i++) {
            push(prime);
        }
    }

    public double[] push(double[] X) {
        // Send the input signals into the network
        for (int i = 0; i < X.length; i++) {
            inputs[i].receive(X[i]);
        }
        // Send the bias signals into the network
        for (int i = 0; i < biases.length; i++) {
            biases[i].receive(BIAS_SIGNAL);
        }
        // Flush the receivers of all the neurons
        Iterator<Neuron> neurons = network.values().iterator();
        while (neurons.hasNext()) {
            neurons.next().flush();
        }
        // Forward the neuron signals
        neurons = network.values().iterator();
        while (neurons.hasNext()) {
            neurons.next().forward();
        }
        // Retrieve output signals
        double[] Y = new double[outputs.length];
        for (int i = 0; i < Y.length; i++) {
            Y[i] = outputs[i].val();
        }
        return Y;
    }

    /**
     * @return a string representation
     */
    @Override
    public String toString() {
        Iterator<Neuron> neurons = network.values().iterator();
        StringBuilder sb = new StringBuilder();
        sb.append("Neural Network:").append('\n');
        while (neurons.hasNext()) {
            sb.append(neurons.next().toString());
        }
        return sb.toString();
    }

}
