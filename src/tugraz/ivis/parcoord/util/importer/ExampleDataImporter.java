package tugraz.ivis.parcoord.util.importer;

import tugraz.ivis.parcoord.util.data.ChartData;
import tugraz.ivis.parcoord.util.data.Record;

import java.util.ArrayList;

// simple DataImport implementation
// can be used for retrieving test/example dataset
public class ExampleDataImporter extends DataImporter {
    class Person {
        String name;
        int age;
        int height;
        float weight;

        Person(String name, int age, int height, float weight) {
            this.name = name;
            this.age = age;
            this.height = height;
            this.weight = weight;
        }
    }

    // exampledata
    Person rawData[] = {new Person("Person1", 13, 150, 34.8f),
            new Person("Person2", 13, 178, 80.0f),
            new Person("Person3", 25, 175, 72.7f),
            new Person("Person4", 36, 199, 92.4f),
            new Person("Person5", 47, 169, 68.2f),
            new Person("Person6", 20, 182, 89.1f),
            new Person("Person7", 81, 185, 78.2f),
            new Person("Person8", 63, 180, 75.6f),
            new Person("Person9", 52, 162, 58.5f)
    };

    @Override
    public String[] getColumnLabels() {
        return new String[]{"Name", "Age", "Height", "Weight"};
    }

    @Override
    public Record[] getRecords() {
        Record records[] = new Record[rawData.length];

        for (int i = 0; i < rawData.length; i++) {
            Person person = rawData[i];
            Object recData[] = {person.name,
                    person.age,
                    person.height,
                    person.weight};
            records[i] = new Record(recData);
        }

        return records;
    }

    // TODO implement to match data to axes?
    // or do this later?
    @Override
    public ArrayList<ChartData> getChartData() {
        return null;
    }
}
