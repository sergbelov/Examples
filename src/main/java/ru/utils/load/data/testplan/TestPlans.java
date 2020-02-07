package ru.utils.load.data.testplan;

import java.util.List;

public class TestPlans {
    String className;
    List<TestPlan> testPlanList;

    public TestPlans() {
    }

    public TestPlans(String className, List<TestPlan> testPlanList) {
        this.className = className;
        this.testPlanList = testPlanList;
    }

    public String getClassName() {
        return className;
    }

    public List<TestPlan> getTestPlanList() {
        return testPlanList;
    }
}

