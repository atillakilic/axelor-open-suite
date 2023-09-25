/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.employee;

import com.axelor.apps.hr.db.EmployeeDailyActivityRu;
import com.axelor.exception.AxelorException;

public interface EmployeeDailyActivityService {

  public boolean updateRecordOnSalary(EmployeeDailyActivityRu dailyActivity) throws AxelorException;

  public void setEmployeeAbsentPlan(EmployeeDailyActivityRu dailyActivity) throws AxelorException;
}
