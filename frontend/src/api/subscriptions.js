const BASE_URL = `${import.meta.env.VITE_API_BASE_URL ?? ''}/api/v1/subscriptions`

export class ApiError extends Error {
  constructor(status, problem) {
    super(problem?.detail ?? `Request failed with status ${status}`)
    this.status = status
    this.problem = problem
  }
}

async function request(path = '', options = {}) {
  const response = await fetch(BASE_URL + path, {
    ...options,
    headers: { 'Content-Type': 'application/json', Accept: 'application/json', ...options.headers },
  })

  if (!response.ok) {
    // errors come back as RFC 9457 problem details
    const problem = await response.json().catch(() => null)
    throw new ApiError(response.status, problem)
  }

  return response.status === 204 ? null : response.json()
}

export const getSubscriptions = () => request()

export const getCount = () => request('/count').then((body) => body.count)

export const getTotal = () => request('/total').then((body) => body.amount)

export const createSubscription = (subscription) =>
  request('', { method: 'POST', body: JSON.stringify(subscription) })

export const updateSubscription = (id, subscription) =>
  request(`/${id}`, { method: 'PUT', body: JSON.stringify(subscription) })

export const deleteSubscription = (id) => request(`/${id}`, { method: 'DELETE' })
