import java.util.*;

public class Solution {
    public static void main(String[] args) {
        ConsoleProcessor processor = new ConsoleProcessor();
        processor.processAllLines();
    }
}

class ConsoleProcessor {
    public OrgChart orgChart = new OrgChart();

    public void processAllLines() {
        Scanner in = new Scanner(System.in);
        String line = in .nextLine();

        Integer numLines = 0;

        try {
            numLines = Integer.valueOf(line.trim());
        }
        catch (NumberFormatException ex) {
            ex.printStackTrace();
        }


        for (int i = 0; i < numLines; i++) {
            processLine( in .nextLine());
        }

        in .close();
    }

    protected void processLine(String line) {
        String[] parsedCommand = line.split(",");

        // ignore empty lines
        if (parsedCommand.length == 0) {
            return;
        }

        switch (parsedCommand[0]) {
            case "add":
                orgChart.add(parsedCommand[1], parsedCommand[2], parsedCommand[3]);
                break;
            case "print":
                orgChart.print();
                break;
            case "remove":
                orgChart.remove(parsedCommand[1]);
                break;
            case "move":
                orgChart.move(parsedCommand[1], parsedCommand[2]);
                break;
            case "count":
                System.out.println(orgChart.count(parsedCommand[1]));
                break;
        }
    }
}


interface CompanyEmployee {
    String getId();
    String getManagerId();
    void setManagerId(String managerId);
    Collection<CompanyEmployee> getReports();
    void addEmployee(CompanyEmployee employee);
    void removeReport(String reportId);
}

class Employee implements CompanyEmployee {
    private String id;

    private String managerId;

    private String name;
    private final Collection<CompanyEmployee> reports = new ArrayList<CompanyEmployee>();

    public Employee(String id, String name, String managerId) {
        this.name = name;
        this.id = id;
        this.managerId = managerId;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getManagerId() {
        return this.managerId;
    }

    @Override
    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    @Override
    public Collection<CompanyEmployee> getReports() {
        return this.reports;
    }

    @Override
    public void addEmployee(CompanyEmployee employee) {
        this.reports.add(employee);
    }

    @Override
    public void removeReport(String reportId) {
        this.reports.removeIf(report -> report.getId() == reportId);
    }


    @Override
    public String toString() {
        return name + " " + "[" + id + "]";
    }
}

class OrgChart {

    private static final String NO_MANAGER_ID = "-1";
    private static final String ROOT_ID = "-1"; // same variable value but used in different contexts slightly

    private CompanyEmployee root = new Employee(ROOT_ID, "root", NO_MANAGER_ID);
    public void add(String id, String name, String managerId) {
        CompanyEmployee newEmployee = new Employee(id, name, managerId);
        addEmployee(newEmployee);
    }

    public void print() {
        prefixPrint(root, "");
    }

    public void remove(String employeeId) {
        removeAndReturnEmployee(employeeId);
    }
    public void move(String employeeId, String newManagerId) {
        Optional<CompanyEmployee> removedEmployeeResult = removeAndReturnEmployee(employeeId);
        if (removedEmployeeResult.isEmpty()) {
            return;
        }
        CompanyEmployee removedEmployee = removedEmployeeResult.get();
        removedEmployee.setManagerId(newManagerId);
        addEmployee(removedEmployee);
    }

    public int count(String employeeId) {
        Optional<CompanyEmployee> employeeFindResult = findEmployee(root, employeeId);
        if (employeeFindResult.isEmpty()) {
            return 0;
        }

        return countAll(employeeFindResult.get());
    }

    private void addEmployee(CompanyEmployee newEmployee) {
        if (newEmployee.getManagerId() == NO_MANAGER_ID) {
            root.addEmployee(newEmployee);
            return;
        }
        // dfs from root till we find the employee with manager ID
        Optional<CompanyEmployee> manager = findEmployee(root, newEmployee.getManagerId());
        if (manager.isPresent()) {
            manager.get().addEmployee(newEmployee);
        } else {
            root.addEmployee(newEmployee);
        }
    }

    private Optional<CompanyEmployee> findEmployee(CompanyEmployee currentEmployeeNode, String employeeIdToFind) {
        if (currentEmployeeNode.getId() == employeeIdToFind) {
            return Optional.of(currentEmployeeNode);
        }
        Collection<CompanyEmployee> reportsFromCurrentEmployee = currentEmployeeNode.getReports();
        if (reportsFromCurrentEmployee.size() == 0) {
            return Optional.empty();
        }
        for (CompanyEmployee employee: reportsFromCurrentEmployee) {
            Optional<CompanyEmployee> employeeFound = findEmployee(employee, employeeIdToFind);
            if (employeeFound.isPresent()) {
                return Optional.of(employeeFound.get());
            }
        }
        return Optional.empty();
    }

    private void prefixPrint(CompanyEmployee employee, String prefix) {
        if (employee.getId() != ROOT_ID) {
            System.out.println(prefix + employee);
        }
        for (CompanyEmployee report : employee.getReports()) {
            prefixPrint(report, ""+ "  ");
        }
    }

    private Optional<CompanyEmployee> removeAndReturnEmployee(String employeeId) {
        Optional<CompanyEmployee> employeeResult = findEmployee(root, employeeId);
        if (employeeResult.isEmpty()) {
            return employeeResult;
        }
        CompanyEmployee employee = employeeResult.get();
        Optional<CompanyEmployee> managerOfEmployee = findEmployee(root, employee.getManagerId());
        if (managerOfEmployee.isEmpty()) {
            return employeeResult;
        }
        managerOfEmployee.get().removeReport(employeeId);
        return employeeResult;
    }

    private int countAll(CompanyEmployee employee) {
        if (employee.getReports().size() == 0) {
            return 0;
        }

        int counts = 0;
        for (CompanyEmployee report : employee.getReports()) {
            counts += countAll(report);
        }
        return 1+counts;
    }
}