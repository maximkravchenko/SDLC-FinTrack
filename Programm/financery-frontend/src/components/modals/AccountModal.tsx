import React, { useState } from 'react';
import { api } from '@/services/api.ts';

const AccountModal = ({ isOpen, onClose, state, dispatch, onOpenUserModal }) => {
  const [showCreateUser, setShowCreateUser] = useState(false);
  const [userName, setUserName] = useState('');
  const [userEmail, setUserEmail] = useState('');

  const { users, currentUser } = state;

  if (!isOpen) return null;

  const handleCreateUser = async (e) => {
    e.preventDefault();
    if (!userName.trim() || !userEmail.trim()) return;

    const newUser = {
      name: userName.trim(),
      email: userEmail.trim(),
    };

    try {
      const createdUser = await api.users.create(newUser);
      dispatch({ type: 'ADD_USER', payload: { id: createdUser.id, name: createdUser.name, email: createdUser.email } });
      dispatch({ type: 'SET_CURRENT_USER', payload: { id: createdUser.id, name: createdUser.name, email: createdUser.email } });
      setUserName('');
      setUserEmail('');
      setShowCreateUser(false);
      onClose();
    } catch (error) {
      console.error(`Failed to create user at ${new Date().toLocaleString('en-US', { timeZone: 'America/Chicago' })}:`, error);
      alert('Ошибка при создании пользователя');
    }
  };

  const handleUserSwitch = async (user) => {
    try {
      dispatch({ type: 'SET_CURRENT_USER', payload: user });
      const accounts = await api.accounts.getByUserId(user.id);
      const formattedAccounts = accounts.map((account) => ({
        id: account.id,
        name: account.name,
        balance: account.balance,
        userId: account.userId,
      }));
      dispatch({ type: 'SET_ACCOUNTS', payload: formattedAccounts });

      const transactions = await api.transactions.getByUserId(user.id);
      const formattedTransactions = transactions.map((transaction) => ({
        id: transaction.id,
        amount: transaction.amount,
        description: transaction.description,
        date: transaction.date,
        type: transaction.type ? 'income' : 'expense',
        userId: transaction.userId,
        accountId: transaction.billId,
        tags: transaction.tags || [],
      }));
      dispatch({ type: 'SET_TRANSACTIONS', payload: formattedTransactions });

      const tags = await api.tags.getByUserId(user.id);
      const formattedTags = tags.map((tag) => ({
        id: tag.id,
        title: tag.title,
        userId: tag.userId,
      }));
      dispatch({ type: 'SET_TAGS', payload: formattedTags });

      onClose();
    } catch (error) {
      console.error(`Failed to switch user at ${new Date().toLocaleString('en-US', { timeZone: 'America/Chicago' })}:`, error);
      alert('Ошибка при переключении пользователя');
    }
  };

  const handleDeleteUser = async () => {
    if (!currentUser?.id) {
      console.error(`No current user ID to delete at ${new Date().toLocaleString('en-US', { timeZone: 'America/Chicago' })}`);
      alert('Ошибка: текущий пользователь не выбран');
      return;
    }

    if (window.confirm('Вы точно хотите удалить пользователя?')) {
      try {
        console.log(`Deleting user with ID: ${currentUser.id} at ${new Date().toLocaleString('en-US', { timeZone: 'America/Chicago' })}`);
        const response = await api.users.delete(currentUser.id); // Получаем полный ответ
        console.log(`User deletion response at ${new Date().toLocaleString('en-US', { timeZone: 'America/Chicago' })}:`, response);

        const updatedUsers = await api.users.getAll();
        dispatch({ type: 'SET_USERS', payload: updatedUsers });

        const remainingUsers = updatedUsers.filter((u) => u.id !== currentUser.id);
        if (remainingUsers.length > 0) {
          const otherUser = remainingUsers[0];
          await handleUserSwitch(otherUser);
        } else {
          dispatch({ type: 'SET_CURRENT_USER', payload: null });
          dispatch({ type: 'SET_ACCOUNTS', payload: [] });
          dispatch({ type: 'SET_TRANSACTIONS', payload: [] });
          dispatch({ type: 'SET_TAGS', payload: [] });
          onClose();
        }

        window.location.reload();
      } catch (error) {
        console.error(`Failed to delete user at ${new Date().toLocaleString('en-US', { timeZone: 'America/Chicago' })}:`, error);
        alert(`Ошибка при удалении пользователя: ${error.message}`);
      }
    }
  };

  return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
        <div className="bg-white rounded-lg max-w-md w-full flex flex-col max-h-[90vh]">
          <div className="p-6 border-b border-gray-200 sticky top-0 bg-white z-10">
            <div className="flex justify-between items-center">
              <h2 className="text-xl font-semibold text-gray-800">Доступные аккаунты</h2>
              <button onClick={onClose} className="text-gray-400 hover:text-gray-600 transition-colors">
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>
          <div className="p-6 overflow-y-auto">
            {currentUser && (
                <div className="mb-6">
                  <h3 className="text-lg font-medium text-gray-800 mb-3">Открытый аккаунт</h3>
                  <div className="bg-[#003464] text-white p-4 rounded-lg">
                    <p className="font-medium">{currentUser.name}</p>
                    <p className="text-white/80">{currentUser.email}</p>
                    <div className="mt-3 flex space-x-2">
                      <button
                          onClick={onOpenUserModal}
                          className="bg-white text-[#003464] px-3 py-1 rounded text-sm hover:bg-gray-100 transition-colors"
                      >
                        Настройка
                      </button>
                      <button
                          onClick={handleDeleteUser}
                          className="bg-red-600 text-white px-3 py-1 rounded text-sm hover:bg-red-700 transition-colors"
                      >
                        Удалить
                      </button>
                    </div>
                  </div>
                </div>
            )}
            <div className="mb-6">
              <h3 className="text-lg font-medium text-gray-800 mb-3">Переключить на</h3>
              <div className="space-y-2">
                {users
                    .filter((user) => user.id !== currentUser?.id)
                    .map((user) => (
                        <button
                            key={user.id}
                            onClick={() => handleUserSwitch(user)}
                            className="w-full text-left p-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                        >
                          <p className="font-medium">{user.name}</p>
                          <p className="text-gray-600">{user.email}</p>
                        </button>
                    ))}
              </div>
            </div>
            <div>
              {!showCreateUser ? (
                  <button
                      onClick={() => setShowCreateUser(true)}
                      className="w-full bg-[#003464] text-white p-3 rounded-lg hover:bg-blue-700 transition-colors"
                  >
                    Добавить аккаунт
                  </button>
              ) : (
                  <form onSubmit={handleCreateUser} className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Имя</label>
                      <input
                          type="text"
                          value={userName}
                          onChange={(e) => setUserName(e.target.value)}
                          className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                          placeholder="Василий Пупович"
                          required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Почта</label>
                      <input
                          type="email"
                          value={userEmail}
                          onChange={(e) => setUserEmail(e.target.value)}
                          className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                          placeholder="example@mail.com"
                          required
                      />
                    </div>
                    <div className="flex space-x-2">
                      <button
                          type="submit"
                          className="flex-1 bg-[#003464] text-white py-2 rounded-lg hover:bg-blue-700 transition-colors"
                      >
                        Создать
                      </button>
                      <button
                          type="button"
                          onClick={() => {
                            setShowCreateUser(false);
                            setUserName('');
                            setUserEmail('');
                          }}
                          className="flex-1 border border-gray-300 text-gray-700 py-2 rounded-lg hover:bg-gray-50 transition-colors"
                      >
                        Отмена
                      </button>
                    </div>
                  </form>
              )}
            </div>
          </div>
        </div>
      </div>
  );
};

export default AccountModal;