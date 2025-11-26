import React, { useState, useEffect } from 'react';
import { api } from '@/services/api.ts';

const TagModal = ({ isOpen, onClose, tag, state, dispatch }) => {
  const [tagTitle, setTagTitle] = useState('');
  const [isEditing, setIsEditing] = useState(false);

  const { currentUser } = state;

  useEffect(() => {
    if (isOpen) {
      if (tag) {
        setIsEditing(true);
        setTagTitle(tag.title);
      } else {
        setIsEditing(false);
        setTagTitle('');
      }
    }
  }, [isOpen, tag]);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!tagTitle.trim()) return;

    const tagData = {
      title: tagTitle.trim(),
      userId: currentUser.id,
    };

    try {
      if (isEditing) {
        const updatedTag = await api.tags.update(tag.id, tagData);
        dispatch({
          type: 'UPDATE_TAG',
          payload: { id: tag.id, title: updatedTag.title, userId: updatedTag.userId },
        });
      } else {
        const newTag = await api.tags.create(tagData);
        dispatch({
          type: 'ADD_TAG',
          payload: { id: newTag.id, title: newTag.title, userId: newTag.userId },
        });
      }
      onClose();
    } catch (error) {
      console.error('Failed to save tag:', error);
      alert('Ошибка при сохранении тега');
    }
  };

  return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
        <div className="bg-white rounded-lg max-w-md w-full">
          <div className="p-6 border-b border-gray-200">
            <div className="flex justify-between items-center">
              <h2 className="text-xl font-semibold text-gray-800">{isEditing ? 'Изменить' : 'Создать'}</h2>
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
              <label className="block text-sm font-medium text-gray-700 mb-1 pl-3">Название</label>
              <input
                  type="text"
                  value={tagTitle}
                  onChange={(e) => setTagTitle(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Введите название тега"
                  required
              />
            </div>

            <div className="flex space-x-2 pt-4">
              <button
                  type="submit"
                  className="flex-1 bg-[#003464] text-white py-2 rounded-lg hover:bg-blue-700 transition-colors"
              >
                {isEditing ? 'Обновить' : 'Создать новый'} Тег
              </button>
              <button
                  type="button"
                  onClick={onClose}
                  className="flex-1 border border-gray-300 text-gray-700 py-2 rounded-lg hover:bg-gray-50 transition-colors"
              >
                Отменя
              </button>
            </div>
          </form>
        </div>
      </div>
  );
};

export default TagModal;