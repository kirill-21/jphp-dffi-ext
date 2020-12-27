<?php

namespace system;

/**
 *
 * Class DFFIKernel32
 * @package system
 */
class DFFIKernel32
{
    /**
     * @param bool $isNtEnabled
     * @return void
     */
    public static function setIsNtEnabled(bool $isNtEnabled)
    {
    }

    /**
     * @return int
     */
    public static function getModuleEntry32WSize(): int {
    }

    /**
     * @param int $hProcess
     * @param int $lpBaseAddress
     * @param int $value
     * @return bool
     */
    public static function writeMemoryInt(int $hProcess, int $lpBaseAddress, int $value): bool
    {
    }

    /**
     * @param int $hProcess
     * @param int $lpBaseAddress
     * @param int $value
     * @return bool
     */
    public static function writeMemoryLong(int $hProcess, int $lpBaseAddress, int $value): bool
    {
    }

    /**
     * @param int $hProcess
     * @param int $lpBaseAddress
     * @param string $value
     * @return bool
     */
    public static function writeMemoryString(int $hProcess, int $lpBaseAddress, string $value): bool
    {
    }

    /**
     * @param int $hProcess
     * @param int $lpBaseAddress
     * @param $value
     * @return bool
     * @deprecated
     */
    public static function writeMemoryValue(int $hProcess, int $lpBaseAddress, $value): bool
    {
    }
}