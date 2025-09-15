package thesis.solver.core;

import org.junit.jupiter.api.Test;
import thesis.model.domain.components.ClassUnit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class VariableTest {
    @Test
    public void testEquals() {
        ClassUnit cls1 = new ClassUnit("1");
        ClassUnit cls2 = new ClassUnit("2");
        ClassUnit cls3 = new ClassUnit("3");

        DefaultISGVariable variable1 = new DefaultISGVariable(cls1, false);
        DefaultISGVariable anotherVariable1 = new DefaultISGVariable(cls1, true);
        DefaultISGVariable variable2 = new DefaultISGVariable(cls2, false);
        DefaultISGVariable variable3 = new DefaultISGVariable(cls3, false);

        assertEquals(variable1, anotherVariable1);

        List<DefaultISGVariable> variableList = List.of(variable1, variable2, variable3);
        for(int i=0; i < variableList.size() - 1; i++) {
            for(int j=i+1; j < variableList.size(); j++) {
                assertNotEquals(variableList.get(i), variableList.get(j));
            }
        }
    }
}
