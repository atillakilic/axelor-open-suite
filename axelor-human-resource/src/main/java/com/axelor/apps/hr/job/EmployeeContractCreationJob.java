package com.axelor.apps.hr.job;

import com.axelor.apps.hr.db.EmployeeContractRu;
import com.axelor.apps.hr.db.EmployeeSalaryRu;
import com.axelor.apps.hr.db.repo.EmployeeContractRuRepository;
import com.axelor.apps.hr.db.repo.EmployeeSalaryRuRepository;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class EmployeeContractCreationJob implements Job {

  @Transactional
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    // Saperate method
    // click menually for create new contract and
    // create one object for keep record

    EmployeeContractRuRepository employeeContractRepo =
        Beans.get(EmployeeContractRuRepository.class);

    LocalDate currentDate = LocalDate.now();
    int dayOfMonth = currentDate.getDayOfMonth();

    if (dayOfMonth != 1) {
      return;
    }

    List<EmployeeContractRu> employeeContractList =
        Beans.get(EmployeeContractRuRepository.class).all().fetch();
    for (EmployeeContractRu employeeContract : employeeContractList) {

      int comparisonResult = currentDate.compareTo(employeeContract.getCronJobDate());
      List<EmployeeSalaryRu> employeeSalaryList = new ArrayList<>();

      // Deactive all the salary month due to contract end date is smaller then current date
      if (comparisonResult > 0) {
        for (EmployeeSalaryRu employeeSalaryRu : employeeContract.getEmployeeSalary()) {
          if (employeeSalaryRu.getCurrentlyActive()) {
            employeeSalaryRu.setCurrentlyActive(false);
            Beans.get(EmployeeSalaryRuRepository.class).save(employeeSalaryRu);
          }
          employeeSalaryList.add(employeeSalaryRu);
        }
        //				 employeeContract.setEmployeeSalary(employeeSalaryList);
        //				 employeeContractRepo.save(employeeContract);
      }
      // Create new salary month from last active salary month and deactive last salary month
      else {
        boolean currentMonthSalaryCreate = true;
        EmployeeSalaryRu lastActiveSalaryMonth = null;
        EmployeeSalaryRu randomSalaryMonth = null;
        for (EmployeeSalaryRu employeeSalaryRu : employeeContract.getEmployeeSalary()) {
          if (employeeSalaryRu.getCurrentlyActive()) {
            lastActiveSalaryMonth = employeeSalaryRu;
            employeeSalaryRu.setCurrentlyActive(false);
            Beans.get(EmployeeSalaryRuRepository.class).save(employeeSalaryRu);
          }
          // if alredy current contract has salary month created
          if (employeeSalaryRu
              .getDateOfStart()
              .toString()
              .equals(currentDate.withDayOfMonth(1).toString())) {
            currentMonthSalaryCreate = false;
          }
          randomSalaryMonth = employeeSalaryRu;
          //					 employeeSalaryList.add(employeeSalaryRu);
        }

        if (lastActiveSalaryMonth == null) {
          lastActiveSalaryMonth = randomSalaryMonth;
        }
        if (lastActiveSalaryMonth != null && currentMonthSalaryCreate) {
          EmployeeSalaryRu newSalaryMonth = new EmployeeSalaryRu();
          newSalaryMonth.setEmployeeContract(lastActiveSalaryMonth.getEmployeeContract());
          newSalaryMonth.setEmployeeRu(lastActiveSalaryMonth.getEmployeeRu());
          newSalaryMonth.setSalaryType(lastActiveSalaryMonth.getSalaryType());
          newSalaryMonth.setFixSalary(lastActiveSalaryMonth.getFixSalary());
          newSalaryMonth.setHourlyRate(lastActiveSalaryMonth.getHourlyRate());
          newSalaryMonth.setSalaryStatus(1);
          newSalaryMonth.setDateOfStart(currentDate.withDayOfMonth(1));
          String date = currentDate.withDayOfMonth(1).toString();
          String employeeName = newSalaryMonth.getEmployeeRu().getName();
          newSalaryMonth.setSalaryInfo(employeeName + "_" + date);
          newSalaryMonth.setCurrentlyActive(true);
          Beans.get(EmployeeSalaryRuRepository.class).save(newSalaryMonth);
          //					 employeeSalaryList.add(newSalaryMonth);
        }
        //				 employeeContract.setEmployeeSalary(employeeSalaryList);
        //				 employeeContractRepo.save(employeeContract);
      }
    }
  }
}
