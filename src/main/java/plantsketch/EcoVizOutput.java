
// this file will compile all the data from the sim and output the .pdb file for EcoViz

package plantsketch;


import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;

public class EcoVizOutput {
    
    int dimx;
    int dimy;
    int numTrees;
    ArrayList<SpeciesMap> overallSpeciesMap;

    public EcoVizOutput(SimulationResult result) {
        this.dimx = result.dimX();
        this.dimy = result.dimY();
        this.numTrees = result.forest().getAllPlants().size();
        this.overallSpeciesMap = result.forest().getOverallSpeciesMap();
    }

    // methods

    public void createFile()
    {
        try (PrintWriter writer = new PrintWriter("output.pdb")) 
        {
            // header
            writer.println("3.0");
            writer.println(dimx + " " + dimy);
            writer.println("0");
            writer.println(numTrees);

            // body
            for (SpeciesMap speciesMap: overallSpeciesMap)
            {
                for (Plant p: speciesMap.getPlants())
                {
                    writer.printf("%d %-4s %.2f %.2f %.2f %.2f 0 0", p.getId(), p.getMnemonic(), p.getX(), p.getY(), p.getHeight(), p.getCanopyRadius());
                }
            }





        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
/*
 
    3.0
    dimx dimy
    0
    numTrees
    TreeID(int) speciesName(4xchar) posx posy height canopyRadius 0 0

 */