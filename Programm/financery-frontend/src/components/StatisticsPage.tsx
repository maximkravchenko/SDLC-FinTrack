import React, { useState } from 'react';
import { PieChart, Pie, Cell, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const StatisticsPage = ({ state, dispatch }) => {
  const [chartType, setChartType] = useState('income'); // 'income' or 'expense'
  const [periodFilter, setPeriodFilter] = useState('all'); // For PieChart
  const [accountFilter, setAccountFilter] = useState('all');
  const [lineChartPeriod, setLineChartPeriod] = useState('month'); // Day, Week, Month, Year

  const { transactions, tags, accounts, currentUser } = state;

  // Filter data for current user
  const userTransactions = transactions.filter((t) => t.userId === currentUser?.id);
  const userTags = tags.filter((tag) => tag.userId === currentUser?.id);
  const userAccounts = accounts.filter((account) => account.userId === currentUser?.id);

  // Debugging
  console.log('User transactions:', userTransactions);
  console.log('User tags:', userTags);
  console.log('User accounts:', userAccounts);

  // Parse date from dd.MM.yyyy to Date object
  const parseDate = (dateStr) => {
    if (!dateStr || !dateStr.includes('.')) return new Date();
    const [day, month, year] = dateStr.split('.');
    return new Date(`${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`);
  };

  // Filter transactions based on selected filters (for PieChart)
  const filteredTransactions = userTransactions.filter((transaction) => {
    // Period filter
    if (periodFilter === 'month') {
      const now = new Date();
      const transactionDate = parseDate(transaction.date);
      if (
          transactionDate.getMonth() !== now.getMonth() ||
          transactionDate.getFullYear() !== now.getFullYear()
      ) {
        return false;
      }
    }

    // Account filter
    if (accountFilter !== 'all' && transaction.accountId !== parseInt(accountFilter)) {
      return false;
    }

    return true;
  });

  // Prepare pie chart data
  const pieChartData = () => {
    console.log('Filtered transactions for PieChart:', filteredTransactions);
    const typeTransactions = filteredTransactions.filter((t) => {
      const transactionType = typeof t.type === 'boolean' ? (t.type ? 'income' : 'expense') : t.type;
      return transactionType === chartType;
    });
    console.log('Type transactions:', typeTransactions);

    const tagData = {};

    typeTransactions.forEach((transaction) => {
      const tags = Array.isArray(transaction.tags) && transaction.tags.length ? transaction.tags : [{ id: 0, title: 'No Tag' }];
      tags.forEach((tag) => {
        const tagTitle = tag.title || 'Unknown Tag';
        if (!tagData[tagTitle]) {
          tagData[tagTitle] = 0;
        }
        tagData[tagTitle] += Math.abs(Number(transaction.amount) || 0);
      });
    });

    console.log('Tag data:', tagData);

    const data = Object.entries(tagData).map(([name, value]) => ({
      name,
      value: Number(value),
      color: getRandomColor(),
    }));

    return data.length ? data : [{ name: 'No Data', value: 1, color: '#E5E7EB' }];
  };

  // Prepare line chart data
  const lineChartData = () => {
    const now = new Date(); // Current date and time: 07:14 PM CDT, May 28, 2025
    const data = [];

    if (lineChartPeriod === 'day') {
      // Last 24 hours, by hour
      for (let i = 23; i >= 0; i--) {
        const date = new Date(now);
        date.setHours(now.getHours() - i, 0, 0, 0);
        const dateStr = `${date.getDate().toString().padStart(2, '0')}.${(date.getMonth() + 1)
            .toString()
            .padStart(2, '0')}.${date.getFullYear()} ${date.getHours().toString().padStart(2, '0')}:00`;

        const hourTransactions = filteredTransactions.filter((t) => {
          const tDate = parseDate(t.date);
          return (
              tDate.getFullYear() === date.getFullYear() &&
              tDate.getMonth() === date.getMonth() &&
              tDate.getDate() === date.getDate() &&
              tDate.getHours() === date.getHours()
          );
        });

        const income = hourTransactions
            .filter((t) => {
              const transactionType = typeof t.type === 'boolean' ? (t.type ? 'income' : 'expense') : t.type;
              return transactionType === 'income';
            })
            .reduce((sum, t) => sum + Number(t.amount), 0);
        const expenses = Math.abs(
            hourTransactions
                .filter((t) => {
                  const transactionType = typeof t.type === 'boolean' ? (t.type ? 'income' : 'expense') : t.type;
                  return transactionType === 'expense';
                })
                .reduce((sum, t) => sum + Number(t.amount), 0)
        );

        data.push({
          time: date.getHours(),
          income,
          expenses,
          index: 23 - i,
        });
      }
    } else if (lineChartPeriod === 'week') {
      // Last 7 days, 8-hour intervals (21 points)
      for (let i = 20; i >= 0; i--) {
        const date = new Date(now);
        date.setHours(now.getHours() - i * 8, 0, 0, 0);
        const dateStr = `${date.getDate().toString().padStart(2, '0')}.${(date.getMonth() + 1)
            .toString()
            .padStart(2, '0')}.${date.getFullYear()} ${date.getHours().toString().padStart(2, '0')}:00`;

        const intervalTransactions = filteredTransactions.filter((t) => {
          const tDate = parseDate(t.date);
          const start = new Date(date);
          start.setHours(date.getHours() - 8);
          return tDate >= start && tDate <= date;
        });

        const income = intervalTransactions
            .filter((t) => {
              const transactionType = typeof t.type === 'boolean' ? (t.type ? 'income' : 'expense') : t.type;
              return transactionType === 'income';
            })
            .reduce((sum, t) => sum + Number(t.amount), 0);
        const expenses = Math.abs(
            intervalTransactions
                .filter((t) => {
                  const transactionType = typeof t.type === 'boolean' ? (t.type ? 'income' : 'expense') : t.type;
                  return transactionType === 'expense';
                })
                .reduce((sum, t) => sum + Number(t.amount), 0)
        );

        data.push({
          time: `${date.getDate().toString().padStart(2, '0')}.${(date.getMonth() + 1)
              .toString()
              .padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:00`,
          income,
          expenses,
          index: 20 - i,
        });
      }
    } else if (lineChartPeriod === 'month') {
      // Last 30 days, by day
      for (let i = 29; i >= 0; i--) {
        const date = new Date(now);
        date.setDate(now.getDate() - i);
        const dateStr = `${date.getDate().toString().padStart(2, '0')}.${(date.getMonth() + 1)
            .toString()
            .padStart(2, '0')}.${date.getFullYear()}`;

        const dayTransactions = filteredTransactions.filter(
            (t) =>
                t.date === dateStr ||
                parseDate(t.date).toISOString().split('T')[0] === date.toISOString().split('T')[0]
        );

        const income = dayTransactions
            .filter((t) => {
              const transactionType = typeof t.type === 'boolean' ? (t.type ? 'income' : 'expense') : t.type;
              return transactionType === 'income';
            })
            .reduce((sum, t) => sum + Number(t.amount), 0);
        const expenses = Math.abs(
            dayTransactions
                .filter((t) => {
                  const transactionType = typeof t.type === 'boolean' ? (t.type ? 'income' : 'expense') : t.type;
                  return transactionType === 'expense';
                })
                .reduce((sum, t) => sum + Number(t.amount), 0)
        );

        data.push({
          time: date.getDate(),
          income,
          expenses,
          index: 29 - i,
        });
      }
    } else if (lineChartPeriod === 'year') {
      // Last 12 months, by month
      for (let i = 11; i >= 0; i--) {
        const date = new Date(now);
        date.setMonth(now.getMonth() - i, 1);
        const monthStart = new Date(date.getFullYear(), date.getMonth(), 1);
        const monthEnd = new Date(date.getFullYear(), date.getMonth() + 1, 0);

        const monthTransactions = filteredTransactions.filter((t) => {
          const tDate = parseDate(t.date);
          return tDate >= monthStart && tDate <= monthEnd;
        });

        const income = monthTransactions
            .filter((t) => {
              const transactionType = typeof t.type === 'boolean' ? (t.type ? 'income' : 'expense') : t.type;
              return transactionType === 'income';
            })
            .reduce((sum, t) => sum + Number(t.amount), 0);
        const expenses = Math.abs(
            monthTransactions
                .filter((t) => {
                  const transactionType = typeof t.type === 'boolean' ? (t.type ? 'income' : 'expense') : t.type;
                  return transactionType === 'expense';
                })
                .reduce((sum, t) => sum + Number(t.amount), 0)
        );

        data.push({
          time: `${(date.getMonth() + 1).toString().padStart(2, '0')}.${date.getFullYear()}`,
          income,
          expenses,
          index: 11 - i,
        });
      }
    }

    console.log('Line chart data:', data);
    return data;
  };

  const getRandomColor = () => {
    const colors = [
      '#003464',
      '#02396B',
      '#043D72',
      '#074279',
      '#094780',
      '#0B4C87',
      '#0D508F',
      '#105596',
      '#125A9D',
      '#145EA4',
      '#1663AB',
      '#1968B2',
      '#1B6DB9',
      '#1D71C0',
      '#1F76C7',
      '#227BCE',
      '#247FD6',
      '#2684DD',
      '#2889E4',
      '#2B8EEB',
      '#2D92F2',
      '#2F97F9',
    ];
    return colors[Math.floor(Math.random() * colors.length)];
  };

  const COLORS = [
    '#003464',
    '#02396B',
    '#043D72',
    '#074279',
    '#094780',
    '#0B4C87',
    '#0D508F',
    '#105596',
    '#125A9D',
    '#145EA4',
    '#1663AB',
    '#1968B2',
    '#1B6DB9',
    '#1D71C0',
    '#1F76C7',
    '#227BCE',
    '#247FD6',
    '#2684DD',
    '#2889E4',
    '#2B8EEB',
    '#2D92F2',
    '#2F97F9',
  ];

  if (!currentUser) {
    return (
        <div className="text-center py-8">
          <p className="text-gray-500">Для продоложения выберите пользователя</p>
        </div>
    );
  }

  return (
      <div className="max-w-6xl mx-auto space-y-8">
        <div className="bg-white rounded-lg shadow-sm border p-6">
          <h1 className="text-2xl font-bold text-gray-800 mb-2">Статистика</h1>
          <p className="text-sm text-gray-600 mb-6">Статистика по финансам для {currentUser.name}</p>
        </div>

        {/* Pie Chart */}
        <div className="bg-white rounded-lg shadow-sm border p-6">
          {/* Filters for PieChart */}
          <div className="flex flex-wrap gap-4 mb-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Период</label>
              <select
                  value={periodFilter}
                  onChange={(e) => setPeriodFilter(e.target.value)}
                  className="border border-gray-300 rounded-lg px-3 py-2 text-sm"
              >
                <option value="all">Все время</option>
                <option value="month">Этот месяц</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Счет</label>
              <select
                  value={accountFilter}
                  onChange={(e) => setAccountFilter(e.target.value)}
                  className="border border-gray-300 rounded-lg px-3 py-2 text-sm"
              >
                <option value="all">Все счета</option>
                {userAccounts.map((account) => (
                    <option key={account.id} value={account.id}>
                      {account.name}
                    </option>
                ))}
              </select>
            </div>
          </div>
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-semibold text-gray-800">Транзакции по тегам</h2>
            <div className="flex space-x-2">
              <button
                  onClick={() => setChartType('income')}
                  className={`px-4 py-2 rounded-lg transition-colors ${
                      chartType === 'income'
                          ? 'bg-[#003464] text-white'
                          : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}
              >
                Доходы
              </button>
              <button
                  onClick={() => setChartType('expense')}
                  className={`px-4 py-2 rounded-lg transition-colors ${
                      chartType === 'expense'
                          ? 'bg-[#003464] text-white'
                          : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}
              >
                Расходы
              </button>
            </div>
          </div>

          <div className="h-96">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                    data={pieChartData()}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                    outerRadius={120}
                    fill="#8884d8"
                    dataKey="value"
                >
                  {pieChartData().map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color || COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(value) => [`${value.toLocaleString()} BYN`, 'Сумма']} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Line Chart */}
        <div className="bg-white rounded-lg shadow-sm border p-6">
          {/* Filter for LineChart Period */}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1 pl-2">Период</label>
            <select
                value={lineChartPeriod}
                onChange={(e) => setLineChartPeriod(e.target.value)}
                className="border border-gray-300 rounded-lg px-3 py-2 text-sm"
            >
              <option value="day">За день</option>
              <option value="week">За неделю</option>
              <option value="month">За Месяц</option>
              <option value="year">За Год</option>
            </select>
          </div>

          <h2 className="text-xl font-semibold text-gray-800 mb-6">График движения Доходов и расходов</h2>

          <div className="h-96">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={lineChartData()}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis
                    dataKey="time"
                    tick={{ fontSize: 12 }}
                    tickFormatter={(value, index) => {
                      const now = new Date();
                      const dataPoint = lineChartData()[index];
                      if (!dataPoint) return '';
                      if (lineChartPeriod === 'day') {
                        return `${value}:00`;
                      } else if (lineChartPeriod === 'week') {
                        return dataPoint.time;
                      } else if (lineChartPeriod === 'month') {
                        const date = new Date();
                        date.setDate(now.getDate() - (29 - dataPoint.index));
                        return `${date.getDate().toString().padStart(2, '0')}.${(date.getMonth() + 1)
                            .toString()
                            .padStart(2, '0')}`;
                      } else {
                        return dataPoint.time;
                      }
                    }}
                />
                <YAxis tick={{ fontSize: 12 }} tickFormatter={(value) => `$${value}`} />
                <Tooltip
                    formatter={(value, name) => [`${value.toLocaleString()} BYN`, name]}
                    labelFormatter={(label, payload) => {
                      if (payload && payload.length && payload[0].payload) {
                        const index = payload[0].payload.index;
                        const now = new Date(); // Определяем now внутри функции
                        if (lineChartPeriod === 'day') {
                          const hour = 23 - index;
                          return `Время ${hour.toString().padStart(2, '0')}:00`;
                        } else if (lineChartPeriod === 'week') {
                          const date = new Date(now);
                          date.setHours(now.getHours() - (20 - index) * 8);
                          return `${date.getDate().toString().padStart(2, '0')}.${(date.getMonth() + 1)
                              .toString()
                              .padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:00`;
                        } else if (lineChartPeriod === 'month') {
                          const date = new Date(now);
                          date.setDate(now.getDate() - (29 - index));
                          return `День ${date.getDate().toString().padStart(2, '0')}.${(date.getMonth() + 1)
                              .toString()
                              .padStart(2, '0')}`;
                        } else {
                          const date = new Date(now);
                          date.setMonth(now.getMonth() - (11 - index));
                          return `Месяц ${(date.getMonth() + 1).toString().padStart(2, '0')}.${date.getFullYear()}`;
                        }
                      }
                      return '';
                    }}
                />
                <Legend />
                <Line
                    type="monotone"
                    dataKey="income"
                    stroke="#10B981"
                    strokeWidth={2}
                    name="Доходы"
                />
                <Line
                    type="monotone"
                    dataKey="expenses"
                    stroke="#EF4444"
                    strokeWidth={2}
                    name="Расходы"
                />
              </LineChart>
            </ResponsiveContainer>
          </div>


        </div>
      </div>
  );
};

export default StatisticsPage;