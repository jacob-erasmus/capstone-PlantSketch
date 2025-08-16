public class PlantSim{

    public static void main(String[] args){
        System.out.println("Hello and welcome");
        Species boxwood = new Species(
            "Boxwood",       // name
            300,             // lifeSpan
            3.75f,           // sunlightC
            4.25f,           // sunlightR
            27.5f,           // moistureC
            12.5f,           // moistureR
            11.75f,          // temperatureC
            23.35f,          // temperatureR
            0f,              // slopeC
            80f,             // slopeR
            "Red",           // colour
            9f,              // maxHeightOpen
            9f,              // maxHeightClosed
            -5f,             // q
            0.42f,           // radiusMultiplierOpen
            0.42f,           // radiusMultiplierClosed
            0.70f,           // leafTransparency
            15f,             // moistureAbsorbtion
            "L"              // growthPeriod
        );

        boxwood.toString();

    }
}