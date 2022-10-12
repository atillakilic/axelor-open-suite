/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.job;

import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.inject.Beans;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/** An example {@link Job} class that prints a some messages to the stderr. */
public class ValidateEmployeesDocument implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    Beans.get(EmployeeService.class).validateAllEmployeeDocuments();
  }
}
