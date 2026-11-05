const euro = new Intl.NumberFormat('en-IE', { style: 'currency', currency: 'EUR' })

export const formatPrice = (amount) => euro.format(amount)
