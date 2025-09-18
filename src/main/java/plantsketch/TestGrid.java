
// this class is going to be a 2x2 grid tester with stated randomised values
// to test that we are cooking sufficiently
package plantsketch;

import java.util.List;
import java.util.Random;

public class TestGrid 
{
    // instance variables
    int sampleCount = 10;
    float gridSpacing = 2.5f; // in metres
    int dim = 2; // dimensions of the grid (square)
    List<PointSample> pinkNoise; // pink noise samples
    TemperatureMap temp;
    AgeMap age;
    MoistureMap moist;
    SunlightMap sun;
    Terrain terrain;

    // constructor
    public TestGrid()
    {
        // initialise the maps

// TO DO: RANDOMISE GRID VALUES WITHIN REQUIRED RANGES
        temp = new TemperatureMap(dim, dim, gridSpacing, new float[][] { { 15.0f, 20.0f }, { 25.0f,30.0f } });
        age = new AgeMap(dim, dim, gridSpacing, new float[][] { { 1.0f, 2.0f }, { 3.0f, 4.0f } });
        moist = new MoistureMap(dim, dim, gridSpacing, new float[][] { { 0.1f, 0.2f }, { 0.3f, 0.4f } });
        sun = new SunlightMap(dim, dim, gridSpacing, new float[][] { { 100.0f, 200.0f }, { 300.0f, 400.0f } });
        terrain = new Terrain(dim, dim, gridSpacing, new AbioticFactors(moist, temp, sun), new float[][] { { 10.0f, 20.0f }, { 30.0f, 40.0f } });
    }

    // methods

    // generate pink noise
    public List<PointSample> pinkNoise()
    {
        float meters = dim * gridSpacing;
        PinkNoiseSampler sampler = new PinkNoiseSampler(meters, meters, 2.0f, 42L); // figure out what seed means and does
        List<PointSample> samples = sampler.generateSamples(sampleCount);
        this.pinkNoise = samples;
        return samples;
    }

    // how many pink noise samples were generated
    public int getNumPinkNoise()
    {
        return pinkNoise.size();
    }

    // the percentage of samples that were turned into points
    public int getPinkProportion()
    {
        return getNumPinkNoise() / sampleCount;
    }


    // add methods that have pre-loaded values to test the maps

    // add setters to change the values of the maps

    // add setters to change viabilities of species and stuff

    // be able to select species and stuff and turn on and off

    // run method 



}