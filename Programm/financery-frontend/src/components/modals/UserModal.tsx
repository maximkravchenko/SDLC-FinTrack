import React, { useState, useEffect } from 'react';
import { api } from '@/services/api.ts';

const UserModal = ({ isOpen, onClose, state, dispatch }) => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');

  const { currentUser } = state;

  useEffect(() => {
    if (currentUser && isOpen) {
      setName(currentUser.name);
      setEmail(currentUser.email);
    }
  }, [currentUser, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim() || !email.trim()) return;

    const updatedUser = {
      name: name.trim(),
      email: email.trim(),
    };

    try {
      const response = await api.users.update(currentUser.id, updatedUser);
      console.log('Updated user response:', response); // Для отладки
      dispatch({ type: 'SET_CURRENT_USER', payload: { id: currentUser.id, name: response.name, email: response.email } });
      dispatch({ type: 'UPDATE_USER', payload: { id: currentUser.id, name: response.name, email: response.email } });
      onClose();
    } catch (error) {
      console.error('Failed to update user:', error);
      alert('Ошибка при обновлении пользователя');
    }
  };

  return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
        <div className="bg-white rounded-lg max-w-md w-full">
          <div className="p-6 border-b border-gray-200">
            <div className="flex justify-between items-center">
              <h2 className="text-xl font-semibold text-gray-800">Редактирование пользователя</h2>
              <button
                  onClick={onClose}
                  className="text-gray-400 hover:text-gray-600 transition-colors"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="p-6 space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Имя</label>
              <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Василий Пипович"
                  required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Почта</label>
              <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="example@mail.ru"
                  required
              />
            </div>

            <div className="flex space-x-2 pt-4">
              <button
                  type="submit"
                  className="flex-1 bg-[#003464] text-white py-2 rounded-lg hover:bg-blue-700 transition-colors"
              >
                Обновить
              </button>
              <button
                  type="button"
                  onClick={onClose}
                  className="flex-1 border border-gray-300 text-gray-700 py-2 rounded-lg hover:bg-gray-50 transition-colors"
              >
                Отмена
              </button>
            </div>
          </form>
        </div>
      </div>
  );
};

export default UserModal;