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
package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.EmployeeHr;
import com.axelor.inject.Beans;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;

public class EmployeeHrServiceImpl implements EmployeeHrService {

  @Transactional
  public void createTask(EmployeeHr employee) {

    String Empname = employee.getName();
    LocalDate todayDate = LocalDate.now();
    LocalDate expDate = LocalDate.now().plusDays(3);

    TeamTask teamTask = new TeamTask();
    teamTask.setName(Empname + " Employee Created");
    teamTask.setPriority("normal");
    teamTask.setStatus("new");
    teamTask.setTaskDate(todayDate);
    teamTask.setTaskDeadline(expDate);
    teamTask.setTaskDuration(3);
    teamTask.setDescription("New Employee has been created");
    Beans.get(TeamTaskRepository.class).save(teamTask);
  }
}
