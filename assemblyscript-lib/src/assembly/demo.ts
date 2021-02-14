export function fibo(n: i64): i64 {
  if (n < 2) {
    return n
  }
  return fibo(n - 1) + fibo(n - 2)
}
