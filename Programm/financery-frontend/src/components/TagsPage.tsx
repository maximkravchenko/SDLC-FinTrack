
import React, { useState } from 'react';
import TagModal from './modals/TagModal';

const TagsPage = ({ state, dispatch }) => {
  const [showTagModal, setShowTagModal] = useState(false);
  const [editingTag, setEditingTag] = useState(null);

  const { tags, currentUser } = state;

  // Filter tags for current user
  const userTags = tags.filter(tag => tag.userId === currentUser?.id);

  const handleEditTag = (tag) => {
    setEditingTag(tag);
    setShowTagModal(true);
  };

  const handleDeleteTag = (tagId) => {
    if (window.confirm('Are you sure you want to delete this tag?')) {
      dispatch({ type: 'DELETE_TAG', payload: tagId });
    }
  };

  const handleAddTag = () => {
    setEditingTag(null);
    setShowTagModal(true);
  };

  if (!currentUser) {
    return (
      <div className="text-center py-8">
        <p className="text-gray-500">Выберите пользователя для продолжения</p>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      <div className="bg-white rounded-lg shadow-sm border">
        <div className="p-6 border-b border-gray-200">
          <div className="flex justify-between items-center">
            <h1 className="text-2xl font-bold text-gray-800">Коллекция тегов</h1>
            <button
              onClick={handleAddTag}
              className="bg-[#003464] text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              Создать Тег
            </button>
          </div>
          <p className="text-sm text-gray-600 mt-2">Все теги пользователя: {currentUser.name}</p>
        </div>

        <div className="p-6">
          {userTags.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-gray-500">Ntub yt найдены. Создайте свой первый тег!</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {userTags.map(tag => (
                <div
                  key={tag.id}
                  className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
                >
                  <div className="flex items-center justify-between">
                    <h3 className="font-medium text-gray-900">{tag.title}</h3>
                    <div className="flex space-x-2">
                      <button
                        onClick={() => handleEditTag(tag)}
                        className="text-blue-600 hover:text-blue-800 transition-colors"
                      >
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                        </svg>
                      </button>
                      <button
                        onClick={() => handleDeleteTag(tag.id)}
                        className="text-red-600 hover:text-red-800 transition-colors"
                      >
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      <TagModal
        isOpen={showTagModal}
        onClose={() => {
          setShowTagModal(false);
          setEditingTag(null);
        }}
        tag={editingTag}
        state={state}
        dispatch={dispatch}
      />
    </div>
  );
};

export default TagsPage;
