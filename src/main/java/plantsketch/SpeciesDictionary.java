// this class is used to store species data for the simulation
package plantsketch;

public class SpeciesDictionary {

    public SpeciesDictionary() {
        // Constructor
    }

    public Species loadBoxwood() {
        ViabilityParameters boxwoodViabilityParameters = new ViabilityParameters(
                3.75f, 4.25f, 27.5f, 12.5f, 11.75f, 23.35f, 0f, 80f);
        GrowthParameters boxwoodGrowthParameters = new GrowthParameters(9f, 9f,
                -5f, 300f);
        Species boxwood = new Species("Boxwood",
                "buse",
                boxwoodViabilityParameters,
                boxwoodGrowthParameters,
                "Red",
                0.42f,
                0.42f,
                0.70f,
                15f,
                "L",
                0f);
        // System.out.println(boxwood.toString());

        return boxwood;
    }

    public Species loadSnowyMespilus() {
        ViabilityParameters snowyMespilusViabilityParameters = new ViabilityParameters(
                7f, 5f, 31f, 9f, 19.25f, 15.75f, 0f, 80f);
        GrowthParameters snowyMespilusGrowthParameters = new GrowthParameters(6f, 6f,
                -4f, 50f);
        Species snowyMespilus = new Species("Snowy Mespilus",
                "amov",
                snowyMespilusViabilityParameters,
                snowyMespilusGrowthParameters,
                "Blue",
                0.41f,
                0.17f,
                0.52f,
                22f,
                "S",
                0f);
        // System.out.println(snowyMespilus.toString());
        return snowyMespilus;
    }

    public Species loadMountainPine() {
        ViabilityParameters mountainPineViabilityParameters = new ViabilityParameters(
                7f, 5f, 21.5f, 18.5f, 11.75f, 23.25f, 0f, 80f);
        GrowthParameters mountainPineGrowthParameters = new GrowthParameters(15f, 20f,
                -4f, 400f);
        Species mountainPine = new Species("Mountain Pine",
                "pimu",
                mountainPineViabilityParameters,
                mountainPineGrowthParameters,
                "Green",
                0.16f,
                0.14f,
                0.43f,
                3f,
                "L", 0f);
        // System.out.println(mountainPine.toString());
        return mountainPine;
    }

    public Species loadSilverFir() {
        ViabilityParameters silverFirViabilityParameters = new ViabilityParameters(
                5f, 3f, 31f, 9f, 11.75f, 23.25f, 0f, 80f);
        GrowthParameters silverFirGrowthParameters = new GrowthParameters(40f, 50f,
                -6f, 550f);
        Species silverFir = new Species("Silver Fir",
                "abal",
                silverFirViabilityParameters,
                silverFirGrowthParameters,
                "Purple",
                0.12f,
                0.07f,
                0.47f,
                22f,
                "L",
                0f);
        // System.out.println(silverFir.toString());
        return silverFir;
    }

    public Species loadSilverBirch() {
        ViabilityParameters silverBirchViabilityParameters = new ViabilityParameters(
                8.25f, 3.75f, 27.5f, 12.5f, 19.25f, 15.75f, 0f, 70f);
        GrowthParameters silverBirchGrowthParameters = new GrowthParameters(18f, 25f,
                -4f, 120f);
        Species silverBirch = new Species("Silver Birch",
                "bepe",
                silverBirchViabilityParameters,
                silverBirchGrowthParameters,
                "Pink",
                0.2f,
                0.1f,
                0.30f,
                15f,
                "S",
                0f);
        // System.out.println(silverBirch.toString());
        return silverBirch;
    }

    public Species loadSissileOak() {
        ViabilityParameters sissileOakViabilityParameters = new ViabilityParameters(
                5f, 3f, 37.5f, 22.5f, 19.25f, 15.75f, 0f, 70f);
        GrowthParameters sissileOakGrowthParameters = new GrowthParameters(30f, 45f,
                -7f, 600f);
        Species sissileOak = new Species("Sissile Oak",
                "qupe",
                sissileOakViabilityParameters,
                sissileOakGrowthParameters,
                "Yellow",
                0.38f,
                0.21f,
                0.35f,
                15f,
                "S",
                0f);
        // System.out.println(sissileOak.toString());
        return sissileOak;
    }

    public Species loadEuropeanBeech() {
        ViabilityParameters europeanBeechViabilityParameters = new ViabilityParameters(
                5.75f, 6.25f, 37.5f, 22.5f, 19.25f, 15.75f, 0f, 70f);
        GrowthParameters europeanBeechGrowthParameters = new GrowthParameters(35f, 50f,
                -4f, 400f);
        Species europeanBeech = new Species("European Beech",
                "fasy",
                europeanBeechViabilityParameters,
                europeanBeechGrowthParameters,
                "Brown",
                0.3f,
                0.13f,
                0.37f,
                15f,
                "S",
                0f);
        // System.out.println(europeanBeech.toString());
        return europeanBeech;
    }

}
