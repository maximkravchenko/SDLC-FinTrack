const BASE_URL = import.meta.env.REACT_APP_API_URL || 'http://localhost:8080';

interface User {
  id: number;
  name: string;
  [key: string]: any;
}

interface Account {
  id: number;
  name: string;
  balance: number;
  userId: number;
  [key: string]: any;
}

interface Transaction {
  id: number;
  accountId: number;
  userId: number;
  type: 'income' | 'expense';
  amount: number;
  date: string;
  name?: string;
  description?: string;
  tags?: (number | { id: number; title?: string })[];
  [key: string]: any;
}

interface TransactionAPIData {
  id?: number;
  name: string;
  amount: number;
  description: string;
  date: string;
  type: boolean;
  userId: number;
  billId: number;
  tagIds: number[];
  [key: string]: any;
}

interface Tag {
  id: number;
  title: string;
  userId: number;
  [key: string]: any;
}

const normalizeTransactionType = (data: any): any => {
  if (Array.isArray(data)) {
    return data.map(item => normalizeTransactionType(item));
  } else if (data && typeof data === 'object') {
    if ('type' in data) {
      return {
        ...data,
        type: data.type === true ? 'income' : 'expense',
        accountId: data.billId ?? data.accountId, // Нормализуем billId в accountId
      };
    }
    return data;
  }
  return data;
};

const apiCall = async <T>(endpoint: string, options: RequestInit = {}): Promise<T> => {
  try {
    const response = await fetch(`${BASE_URL}${endpoint}`, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
    }

    if (options.method === 'DELETE') {
      const data = await response.json();
      if (data !== 1) {
        throw new Error('Unexpected response from DELETE request: expected 1');
      }
      return data as T;
    }

    const data = await response.json();
    const normalizedData = normalizeTransactionType(data);
    console.log(`Data from ${endpoint}:`, normalizedData);
    return normalizedData as T;
  } catch (error) {
    console.error(`API call failed:`, error);
    throw error;
  }
};

export const api = {
  users: {
    getAll: () => apiCall<User[]>('/users/get-all-users'),
    getById: (id: number) => apiCall<User>(`/users/search-by-id/${id}`),
    create: (userData: Partial<User>) => apiCall<User>('/users/create', {
      method: 'POST',
      body: JSON.stringify(userData),
    }),
    update: (id: number, userData: Partial<User>) => apiCall<User>(`/users/update-by-id/${id}`, {
      method: 'PUT',
      body: JSON.stringify(userData),
    }),
    delete: (id: number) => apiCall<number>(`/users/delete-by-id/${id}`, {
      method: 'DELETE',
    }),
  },
  accounts: {
    getAll: () => apiCall<Account[]>('/bills/get-all-bills'),
    getByUserId: (userId: number) => apiCall<Account[]>(`/bills/get-all-user-bills/${userId}`),
    create: (accountData: Partial<Account>) => apiCall<Account>('/bills/create', {
      method: 'POST',
      body: JSON.stringify(accountData),
    }),
    update: (id: number, accountData: Partial<Account>) => apiCall<Account>(`/bills/update-by-id/${id}`, {
      method: 'PUT',
      body: JSON.stringify(accountData),
    }),
    delete: (id: number) => apiCall<number>(`/bills/delete-by-id/${id}`, {
      method: 'DELETE',
    }),
  },
  transactions: {
    getAll: () => apiCall<Transaction[]>('/transactions/get-all-transactions'),
    getByUserId: (userId: number) => apiCall<Transaction[]>(`/transactions/get-all-user-transactions/${userId}`),
    getByAccountId: (accountId: number) => apiCall<Transaction[]>(`/transactions/get-all-bill-transactions/${accountId}`),
    create: (transactionData: TransactionAPIData) => apiCall<Transaction>('/transactions/create', {
      method: 'POST',
      body: JSON.stringify(transactionData),
    }),
    update: (id: number, transactionData: TransactionAPIData) => apiCall<Transaction>(`/transactions/update-by-id/${id}`, {
      method: 'PUT',
      body: JSON.stringify(transactionData),
    }),
    delete: (id: number) => apiCall<number>(`/transactions/delete-by-id/${id}`, {
      method: 'DELETE',
    }),
  },
  tags: {
    getAll: () => apiCall<Tag[]>('/tags/get-all-tags'),
    getByUserId: (userId: number) => apiCall<Tag[]>(`/tags/get-all-user-tags/${userId}`),
    create: (tagData: Partial<Tag>) => apiCall<Tag>('/tags/create', {
      method: 'POST',
      body: JSON.stringify(tagData),
    }),
    update: (id: number, tagData: Partial<Tag>) => apiCall<Tag>(`/tags/update-by-id/${id}`, {
      method: 'PUT',
      body: JSON.stringify(tagData),
    }),
    delete: (id: number) => apiCall<number>(`/tags/delete-by-id/${id}`, {
      method: 'DELETE',
    }),
  },
};