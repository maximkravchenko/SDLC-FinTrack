import React, { useState, useEffect } from 'react';
import AccountCard from './AccountCard';
import TransactionModal from './modals/TransactionModal';
import TransactionList from './TransactionList';
import { api } from '@/services/api';

// Функция нормализации типов транзакций (копируем из api.ts, чтобы использовать локально)
const normalizeTransactionType = (data: any): any => {
  if (Array.isArray(data)) {
    return data.map(item => normalizeTransactionType(item));
  } else if (data && typeof data === 'object') {
    if ('type' in data) {
      return {
        ...data,
        type: data.type === true ? 'income' : 'expense',
        accountId: data.billId ?? data.accountId,
      };
    }
    return data;
  }
  return data;
};

const HomePage = ({ state, dispatch }) => {
  const [showTransactionModal, setShowTransactionModal] = useState(false);
  const [transactionType, setTransactionType] = useState<'income' | 'expense'>('income');
  const [currentAccountIndex, setCurrentAccountIndex] = useState(0);
  const [showCreateAccountModal, setShowCreateAccountModal] = useState(false);
  const [filters, setFilters] = useState({
    period: 'all',
    type: 'all',
    tags: [],
  });

  const { accounts, transactions, tags, currentUser } = state;

  // Нормализация транзакций при первом рендере
  useEffect(() => {
    if (transactions.length > 0) {
      // Проверяем, есть ли транзакции с ненормализованным типом (true/false)
      const hasUnnormalizedTransactions = transactions.some(
          (t) => typeof t.type === 'boolean'
      );
      if (hasUnnormalizedTransactions) {
        const normalizedTransactions = normalizeTransactionType(transactions);
        dispatch({ type: 'SET_TRANSACTIONS', payload: normalizedTransactions });
      }
    }
  }, [transactions, dispatch]);

  // Filter accounts and transactions for current user
  const userAccounts = accounts.filter((account) => account.userId === currentUser?.id);
  const currentAccount = userAccounts[currentAccountIndex] || null;

  const userTransactions = transactions.filter((t) => t.userId === currentUser?.id);
  const currentAccountTransactions = userTransactions.filter(
      (t) => t.accountId === currentAccount?.id
  );

  // Calculate totals for current user
  const totalBalance = userAccounts.reduce((sum, account) => sum + account.balance, 0);
  const totalIncome = userTransactions
      .filter((t) => t.type === 'income')
      .reduce((sum, t) => sum + t.amount, 0);
  const totalExpenses = Math.abs(
      userTransactions.filter((t) => t.type === 'expense').reduce((sum, t) => sum + t.amount, 0)
  );

  const handlePrevAccount = () => {
    setCurrentAccountIndex((prev) => Math.max(0, prev - 1));
  };

  const handleNextAccount = () => {
    setCurrentAccountIndex((prev) => Math.min(userAccounts.length, prev + 1));
  };

  const handleAddTransaction = (type: 'income' | 'expense') => {
    if (!currentAccount) {
      alert('Пожалуйста, выберите счет');
      return;
    }
    setTransactionType(type);
    setShowTransactionModal(true);
  };

  const handleCreateAccount = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.target as HTMLFormElement);
    const name = formData.get('name');
    const balance = formData.get('balance');

    if (typeof name === 'string' && name.trim() && typeof balance === 'string' && balance.trim()) {
      const newAccount = {
        name: name.trim(),
        balance: parseFloat(balance.trim()),
        userId: currentUser.id,
      };
      try {
        const createdAccount = await api.accounts.create(newAccount);
        dispatch({
          type: 'ADD_ACCOUNT',
          payload: createdAccount,
        });
        setShowCreateAccountModal(false);
        setCurrentAccountIndex(userAccounts.length);
      } catch (error) {
        console.error('Failed to create account:', error);
        alert('Ошибка при создании счета');
      }
    }
  };

  // Синхронизация данных после изменений
  useEffect(() => {
    const syncData = async () => {
      if (currentUser) {
        const updatedAccounts = await api.accounts.getByUserId(currentUser.id);
        const updatedTransactions = await api.transactions.getByUserId(currentUser.id);
        dispatch({ type: 'SET_ACCOUNTS', payload: updatedAccounts });
        dispatch({ type: 'SET_TRANSACTIONS', payload: updatedTransactions });
      }
    };
    syncData();
  }, [currentUser, dispatch]);

  if (!currentUser) {
    return (
        <div className="text-center py-8">
          <p className="text-gray-500">Пожалуйста, выберите пользователя</p>
        </div>
    );
  }

  return (
      <>
        {/* Основной контент страницы */}
        <div className="space-y-8">
          {/* Total Balance and Stats - Centered */}
          <div className="text-center space-y-3">
            <div className="grid grid-cols-1 bg-white max-w-md mx-auto rounded-lg shadow-sm border p-4">
              <h3 className="text-lg font-semibold text-gray-700 mb-2">Общий баланс</h3>
              <p className="text-2xl font-bold text-[#003464]">
                {totalBalance.toLocaleString('be-BY', { minimumFractionDigits: 2 })} BYN
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-md mx-auto">
              <div className="bg-white rounded-lg shadow-sm border p-4 text-center">
                <h3 className="text-m font-semibold text-gray-700 mb-2">Общий доход</h3>
                <p className="text-xl font-bold text-green-600">
                  +{totalIncome.toLocaleString('be-BY', { minimumFractionDigits: 2 })} BYN
                </p>
              </div>
              <div className="bg-white rounded-lg shadow-sm border p-4 text-center">
                <h3 className="text-m font-semibold text-gray-700 mb-2">Общие расходы</h3>
                <p className="text-xl font-bold text-red-600">
                  -{totalExpenses.toLocaleString('be-BY', { minimumFractionDigits: 2 })} BYN
                </p>
              </div>
            </div>
          </div>

          {/* Account Cards Slider - Centered */}
          <div className="bg-white rounded-lg shadow-sm border p-4">
            <h2 className="text-xl font-bold text-gray-800 mb-4 text-center">Счета</h2>

            <div className="flex items-center justify-center space-x-4">
              <button
                  onClick={handlePrevAccount}
                  disabled={currentAccountIndex === 0}
                  className="p-2 rounded-full bg-gray-100 hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </button>

              <div className="flex-1 flex justify-center">
                {currentAccountIndex < userAccounts.length ? (
                    <div className="w-64">
                      <AccountCard
                          account={currentAccount}
                          transactions={currentAccountTransactions}
                          state={state}
                          dispatch={dispatch}
                      />
                    </div>
                ) : (
                    <div
                        onClick={() => setShowCreateAccountModal(true)}
                        className="w-64 bg-gray-100 border-2 border-dashed border-gray-300 rounded-lg flex items-center justify-center cursor-pointer hover:bg-gray-200 transition-colors h-[200px]"
                        style={{ aspectRatio: '1/0.8' }}
                    >
                      <div className="text-center">
                        <svg
                            className="w-12 h-12 mx-auto text-gray-400 mb-2"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                        >
                          <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth={2}
                              d="M12 6v6m0 0v6m0-6h6m-6 0H6"
                          />
                        </svg>
                        <p className="text-gray-500 font-medium">Добавить счет</p>
                      </div>
                    </div>
                )}
              </div>

              <button
                  onClick={handleNextAccount}
                  disabled={currentAccountIndex >= userAccounts.length}
                  className="p-2 rounded-full bg-gray-100 hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </button>
            </div>

            <div className="text-center text-sm text-gray-500 mt-2">
              {userAccounts.length > 0 || currentAccountIndex === userAccounts.length
                  ? `${currentAccountIndex + 1} из ${userAccounts.length + 1} счетов`
                  : 'Нет счетов'}
            </div>
          </div>

          {/* Transaction Buttons - Centered */}
          {currentAccount && (
              <div className="flex justify-center space-x-5">
                <button
                    onClick={() => handleAddTransaction('income')}
                    className="bg-green-600 text-white px-8 py-3 rounded-lg hover:bg-green-700 transition-colors flex items-center space-x-2"
                >
                  <span className="text-xl">+</span>
                  <span>Доход</span>
                </button>
                <button
                    onClick={() => handleAddTransaction('expense')}
                    className="bg-red-600 text-white px-8 py-3 rounded-lg hover:bg-red-700 transition-colors flex items-center space-x-2"
                >
                  <span className="text-xl">−</span>
                  <span>Расход</span>
                </button>
              </div>
          )}

          {/* Current Account Transactions */}
          {currentAccount && (
              <TransactionList
                  transactions={currentAccountTransactions}
                  tags={tags.filter((tag) => tag.userId === currentUser?.id)}
                  filters={filters}
                  setFilters={setFilters}
                  state={state}
                  dispatch={dispatch}
                  title={`Недавние транзакции по счету: ${currentAccount.name}`}
              />
          )}
        </div>

        {/* Модальные окна вынесены за пределы основного div */}
        {showCreateAccountModal && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
              <div className="bg-white rounded-lg max-w-md w-full">
                <div className="p-6 border-b border-gray-200">
                  <div className="flex justify-between items-center">
                    <h2 className="text-xl font-semibold text-gray-800">Создать новый счет</h2>
                    <button
                        onClick={() => setShowCreateAccountModal(false)}
                        className="text-gray-400 hover:text-gray-600 transition-colors"
                    >
                      <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M6 18L18 6M6 6l12 12"
                        />
                      </svg>
                    </button>
                  </div>
                </div>

                <form onSubmit={handleCreateAccount} className="p-6 space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Название счета</label>
                    <input
                        type="text"
                        name="name"
                        className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        placeholder="Введите название счета"
                        required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Начальный баланс</label>
                    <input
                        type="number"
                        step="0.01"
                        name="balance"
                        className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        placeholder="0.00"
                        required
                    />
                  </div>

                  <div className="flex space-x-2 pt-4">
                    <button
                        type="submit"
                        className="flex-1 bg-[#003464] text-white py-2 rounded-lg hover:bg-blue-700 transition-colors"
                    >
                      Создать счет
                    </button>
                    <button
                        type="button"
                        onClick={() => setShowCreateAccountModal(false)}
                        className="px-4 border border-gray-300 text-gray-700 py-2 rounded-lg hover:bg-gray-50 transition-colors"
                    >
                      Отмена
                    </button>
                  </div>
                </form>
              </div>
            </div>
        )}

        {showTransactionModal && (
            <TransactionModal
                isOpen={showTransactionModal}
                onClose={() => setShowTransactionModal(false)}
                type={transactionType}
                account={currentAccount}
                transaction={null}
                state={state}
                dispatch={dispatch}
            />
        )}
      </>
  );
};

export default HomePage;