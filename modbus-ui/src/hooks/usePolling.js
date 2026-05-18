import { useEffect, useRef } from 'react'

/**
 * 지정한 intervalMs 마다 callback 을 반복 호출하는 훅.
 * 컴포넌트 언마운트 시 자동으로 정리된다.
 *
 * 사용 예:
 *   usePolling(() => fetchConfig(), 2000)
 *
 * @param {Function} callback - 반복 실행할 함수 (async 가능)
 * @param {number}   intervalMs - 폴링 주기 (ms), 0이면 비활성
 */
export function usePolling(callback, intervalMs) {
  const savedCallback = useRef(callback)

  // callback이 바뀌어도 최신 버전을 참조
  useEffect(() => {
    savedCallback.current = callback
  }, [callback])

  useEffect(() => {
    if (!intervalMs) return

    // 마운트 즉시 1회 실행
    savedCallback.current()

    const id = setInterval(() => savedCallback.current(), intervalMs)
    return () => clearInterval(id)
  }, [intervalMs])
}
